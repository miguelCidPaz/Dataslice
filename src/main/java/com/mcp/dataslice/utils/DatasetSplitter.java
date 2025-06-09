package com.mcp.dataslice.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.example.GroupReadSupport;
import org.apache.parquet.schema.MessageType;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Utility class for splitting datasets (CSV, JSON, JSONL, Parquet) 
 * into smaller parts for parallel processing.
 */
public class DatasetSplitter {

	/**
     * Splits a CSV file into multiple parts.
     *
     * @param filePath path to the CSV file
     * @param numParts number of parts to split into
     * @return list of split CSV files
     * @throws IOException if reading or writing fails
     */

    public static List<File> splitCsv(String filePath, int numParts) throws IOException {
        File inputFile = new File(filePath);
        String baseName = inputFile.getName().replace(".csv", "");
        String parentDir = inputFile.getParent();

        List<File> partFiles = new ArrayList<>();

        int totalLines = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            reader.readLine();
            while (reader.readLine() != null) totalLines++;
        }

        int linesPerPart = (int) Math.ceil((double) totalLines / numParts);
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String header = reader.readLine();
            int currentPart = 1;
            int currentLineCount = 0;

            File partFile = new File(parentDir + "/" + baseName + "_part" + currentPart + ".csv");
            PrintWriter writer = new PrintWriter(new FileWriter(partFile));
            partFiles.add(partFile);
            writer.println(header);

            String line;
            while ((line = reader.readLine()) != null) {
                if (currentLineCount >= linesPerPart) {
                    writer.close();
                    currentPart++;
                    currentLineCount = 0;
                    partFile = new File(parentDir + "/" + baseName + "_part" + currentPart + ".csv");
                    writer = new PrintWriter(new FileWriter(partFile));
                    partFiles.add(partFile);
                    writer.println(header);
                }
                writer.println(line);
                currentLineCount++;
            }
            writer.close();
        }
        return partFiles;
    }

    /**
     * Splits a JSON array file into multiple parts.
     *
     * @param filePath path to the JSON file
     * @param numParts number of parts to split into
     * @return list of split JSON files
     * @throws IOException if reading or writing fails
     */
    public static List<File> splitJson(String filePath, int numParts) throws IOException {
        File inputFile = new File(filePath);
        String baseName = inputFile.getName().replace(".json", "");
        String parentDir = inputFile.getParent();
        List<File> partFiles = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = new JsonFactory();

        // Primera pasada para contar elementos
        int totalItems = 0;
        try (JsonParser counter = factory.createParser(inputFile)) {
            if (counter.nextToken() != JsonToken.START_ARRAY) {
                throw new IOException("Expected JSON array");
            }
            while (counter.nextToken() != JsonToken.END_ARRAY) {
                counter.skipChildren();
                totalItems++;
            }
        }

        int itemsPerPart = (int) Math.ceil((double) totalItems / numParts);

        // Segunda pasada para repartir en archivos
        try (JsonParser parser = factory.createParser(inputFile)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IOException("Expected JSON array");
            }

            int currentPart = 1;
            int currentCount = 0;
            List<JsonNode> buffer = new ArrayList<>();

            while (parser.nextToken() != JsonToken.END_ARRAY) {
                JsonNode node = mapper.readTree(parser);
                buffer.add(node);
                currentCount++;

                if (currentCount >= itemsPerPart) {
                    writeJsonPart(buffer, parentDir, baseName, currentPart);
                    partFiles.add(new File(parentDir + "/" + baseName + "_part" + currentPart + ".json"));
                    buffer.clear();
                    currentCount = 0;
                    currentPart++;
                }
            }

            if (!buffer.isEmpty()) {
                writeJsonPart(buffer, parentDir, baseName, currentPart);
                partFiles.add(new File(parentDir + "/" + baseName + "_part" + currentPart + ".json"));
            }
        }

        return partFiles;
    }


    /**
     * Helper method to write a buffered list of JsonNodes to a JSON part file.
     *
     * @param buffer list of JsonNode objects
     * @param dir target directory
     * @param baseName base name of the file
     * @param partNumber part index number
     * @throws IOException if writing fails
     */
    private static void writeJsonPart(List<JsonNode> buffer, String dir, String baseName, int partNumber) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File partFile = new File(dir + "/" + baseName + "_part" + partNumber + ".json");
        mapper.writerWithDefaultPrettyPrinter().writeValue(partFile, buffer);
    }

    /**
     * Splits a JSONL (JSON Lines) file into multiple parts.
     *
     * @param filePath path to the JSONL file
     * @param numParts number of parts to split into
     * @return list of split JSONL files
     * @throws IOException if reading or writing fails
     */
    public static List<File> splitJsonl(String filePath, int numParts) throws IOException {
        File inputFile = new File(filePath);
        String baseName = inputFile.getName().replace(".jsonl", "");
        String parentDir = inputFile.getParent();
        List<File> partFiles = new ArrayList<>();

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) lines.add(line);
        }

        int totalLines = lines.size();
        int linesPerPart = (int) Math.ceil((double) totalLines / numParts);
        int currentPart = 1;
        int currentCount = 0;

        File partFile = new File(parentDir + "/" + baseName + "_part" + currentPart + ".jsonl");
        PrintWriter writer = new PrintWriter(new FileWriter(partFile));
        partFiles.add(partFile);

        for (String line : lines) {
            if (currentCount >= linesPerPart) {
                writer.close();
                currentPart++;
                currentCount = 0;
                partFile = new File(parentDir + "/" + baseName + "_part" + currentPart + ".jsonl");
                writer = new PrintWriter(new FileWriter(partFile));
                partFiles.add(partFile);
            }
            writer.println(line);
            currentCount++;
        }
        writer.close();

        return partFiles;
    }

    /**
     * Splits a Parquet file into multiple parts.
     *
     * @param filePath path to the Parquet file
     * @param numParts number of parts to split into
     * @return list of split Parquet files
     * @throws IOException if reading or writing fails
     */
    public static List<File> splitParquet(String filePath, int numParts) throws IOException {
        File inputFile = new File(filePath);
        String baseName = inputFile.getName().replace(".parquet", "");
        String parentDir = inputFile.getParent();

        List<File> partFiles = new ArrayList<>();
        Configuration conf = new Configuration();

        // Detect schema
        MessageType schema = ParquetUtils.detectParquetSchema(filePath, conf);
        int totalRows = 0;

        // Count total rows first
        try (ParquetReader<Group> counter = ParquetReader.builder(new GroupReadSupport(), new Path(filePath)).withConf(conf).build()) {
            while (counter.read() != null) {
                totalRows++;
            }
        }

        int rowsPerPart = (int) Math.ceil((double) totalRows / numParts);
        DataSliceLogger.info("✂ Dividiendo Parquet en " + numParts + " partes, ~" + rowsPerPart + " filas por parte.");

        int currentPart = 1;
        int currentCount = 0;

        try (ParquetReader<Group> reader = ParquetReader.builder(new GroupReadSupport(), new Path(filePath)).withConf(conf).build()) {
            Group record;
            ParquetWriter<Group> writer = ParquetUtils.createParquetWriter(new File(parentDir, baseName + "_part" + currentPart + ".parquet"), conf, schema);
            partFiles.add(new File(parentDir, baseName + "_part" + currentPart + ".parquet"));

            while ((record = reader.read()) != null) {
                writer.write(record);
                currentCount++;

                if (currentCount >= rowsPerPart) {
                    writer.close();
                    currentPart++;
                    if (currentPart > numParts) break; // prevent over-splitting
                    writer = ParquetUtils.createParquetWriter(new File(parentDir, baseName + "_part" + currentPart + ".parquet"), conf, schema);
                    partFiles.add(new File(parentDir, baseName + "_part" + currentPart + ".parquet"));
                    currentCount = 0;
                }
            }

            writer.close();
        }

        DataSliceLogger.info("✅ División Parquet completada.");
        return partFiles;
    }
}
