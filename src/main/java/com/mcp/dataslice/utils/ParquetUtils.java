package com.mcp.dataslice.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.example.ExampleParquetWriter;
import org.apache.parquet.hadoop.example.GroupReadSupport;
import org.apache.parquet.hadoop.example.GroupWriteSupport;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.schema.MessageType;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling Parquet file operations such as reading schemas, splitting files,
 * and converting INT96 timestamps.
 */
public class ParquetUtils {

    /**
     * Reads the Parquet schema from a file.
     *
     * @param conf the Hadoop configuration
     * @param path the path to the Parquet file
     * @return the Parquet MessageType schema
     * @throws IOException if reading fails
     */
    public static MessageType readSchema(Configuration conf, Path path) throws IOException {
        try (ParquetFileReader reader = ParquetFileReader.open(conf, path)) {
            return reader.getFooter().getFileMetaData().getSchema();
        }
    }

    /**
     * Creates a ParquetWriter for writing Group records.
     *
     * @param outputFile the output file
     * @param conf the Hadoop configuration
     * @param schema the Parquet schema
     * @return the ParquetWriter instance
     * @throws IOException if writer creation fails
     */
    public static ParquetWriter<Group> createParquetWriter(File outputFile, Configuration conf, MessageType schema) throws IOException {
        GroupWriteSupport.setSchema(schema, conf);
        return ExampleParquetWriter.builder(new Path(outputFile.getAbsolutePath()))
                .withConf(conf)
                .withType(schema)
                .build();
    }

    /**
     * Splits a Parquet file into multiple smaller Parquet files.
     *
     * @param filePath the path to the original Parquet file
     * @param numParts the number of parts to split into
     * @return a list of File objects representing the split parts
     * @throws IOException if splitting fails
     */
    public static List<File> splitParquet(String filePath, int numParts) throws IOException {
        File inputFile = new File(filePath);
        String baseName = inputFile.getName().replace(".parquet", "");
        String parentDir = inputFile.getParent();
        List<File> partFiles = new ArrayList<>();

        Configuration conf = new Configuration();
        Path path = new Path(filePath);
        MessageType schema = readSchema(conf, path);
        int totalRows = 0;
        List<Group> buffer = new ArrayList<>();

        try (ParquetReader<Group> reader = ParquetReader.builder(new GroupReadSupport(), path).withConf(conf).build()) {
            Group group;
            while ((group = reader.read()) != null) {
                buffer.add(group);
                totalRows++;
            }
        }

        int rowsPerPart = (int) Math.ceil((double) totalRows / numParts);
        int currentPart = 1;
        int currentCount = 0;

        ParquetWriter<Group> writer = null;

        for (Group group : buffer) {
            if (currentCount == 0) {
                File partFile = new File(parentDir + "/" + baseName + "_part" + currentPart + ".parquet");
                partFiles.add(partFile);
                writer = createParquetWriter(partFile, conf, schema);
            }

            writer.write(group);
            currentCount++;

            if (currentCount >= rowsPerPart) {
                writer.close();
                currentPart++;
                currentCount = 0;
            }
        }

        if (writer != null) {
            writer.close();
        }

        DataSliceLogger.info("✅ Parquet splitting completed into " + partFiles.size() + " files.");
        return partFiles;
    }

    /**
     * Converts a Parquet INT96 binary value to an ISO-8601 timestamp string.
     *
     * @param int96Binary the INT96 binary value
     * @return the ISO-8601 formatted timestamp string
     */
    public static String convertInt96ToTimestamp(Binary int96Binary) {
        ByteBuffer buffer = int96Binary.toByteBuffer();

        long nanos = buffer.getLong();
        int julianDay = buffer.getInt();

        long epochMillis = (julianDay - 2440588) * 86400L * 1000;
        long millisOfDay = nanos / 1_000_000;
        epochMillis += millisOfDay;

        Instant instant = Instant.ofEpochMilli(epochMillis);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);

        return dateTime.toString();
    }

    /**
     * Detects the Parquet schema of a file.
     *
     * @param filePath the Parquet file path
     * @param conf the Hadoop configuration
     * @return the detected MessageType schema
     * @throws IOException if reading the schema fails
     */
    public static MessageType detectParquetSchema(String filePath, Configuration conf) throws IOException {
        Path path = new Path(filePath);
        ParquetMetadata metadata = ParquetFileReader.readFooter(conf, path);
        return metadata.getFileMetaData().getSchema();
    }
}
