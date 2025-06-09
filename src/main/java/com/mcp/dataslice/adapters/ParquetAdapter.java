package com.mcp.dataslice.adapters;

import com.mcp.dataslice.utils.DataSliceLogger;
import com.mcp.dataslice.utils.DatasetUtils;
import com.mcp.dataslice.utils.ParquetUtils;
import com.mcp.dataslice.models.InputReadBlock;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.example.GroupReadSupport;

import java.io.IOException;
import java.util.*;

/**
 * Adapter for loading Parquet datasets.
 * Reads Parquet files using Hadoop and Parquet libraries,
 * collects records and columns, batches them, and converts them to InputReadBlock objects.
 */
public class ParquetAdapter implements DatasetAdapter {

    /**
     * Loads a Parquet dataset from the specified file path, batching the records.
     *
     * @param filePath  the path to the Parquet file
     * @param batchSize the maximum number of records per batch
     * @return an Iterable of InputReadBlock
     */
    public Iterable<InputReadBlock> loadDataset(String filePath, int batchSize) {
        List<InputReadBlock> blocks = new ArrayList<>();
        DataSliceLogger.info("📦 [ParquetAdapter] Loading Parquet file: " + filePath);
        String datasetName = DatasetUtils.extractDatasetName(filePath);

        Set<String> columnNames = new LinkedHashSet<>();
        Configuration conf = new Configuration();
        Path path = new Path(filePath);

        List<String[]> block = new ArrayList<>();
        int totalRows = 0;

        try (ParquetReader<Group> reader = ParquetReader.builder(new GroupReadSupport(), path).withConf(conf).build()) {
            Group group;
            while ((group = reader.read()) != null) {
                int fieldCount = group.getType().getFieldCount();
                String[] rowValues = new String[fieldCount];

                for (int i = 0; i < fieldCount; i++) {
                    String colName = group.getType().getFieldName(i);
                    columnNames.add(colName);

                    String value = "";
                    if (group.getFieldRepetitionCount(colName) > 0) {
                        String typeName = group.getType().getType(i).asPrimitiveType().getPrimitiveTypeName().name();

                        if ("INT96".equals(typeName)) {
                            value = ParquetUtils.convertInt96ToTimestamp(group.getInt96(colName, 0));
                        } else {
                            value = group.getValueToString(i, 0);
                        }
                    }
                    rowValues[i] = value.trim();
                }

                block.add(rowValues);
                totalRows++;

                if (block.size() >= batchSize) {
                    blocks.add(buildInputBlock(datasetName, columnNames, block));
                    block.clear();
                }
            }

            if (!block.isEmpty()) {
                blocks.add(buildInputBlock(datasetName, columnNames, block));
            }

            DataSliceLogger.info("[ParquetAdapter] Total records read: " + totalRows);

        } catch (IOException e) {
            DataSliceLogger.error("[ParquetAdapter] ERROR reading file: " + e.getMessage());
            e.printStackTrace();
        }

        return blocks;
    }

    /**
     * Builds an InputReadBlock from the provided rows and column names.
     *
     * @param datasetName the name of the dataset
     * @param columnNames the set of column names
     * @param rows        the list of row data
     * @return an InputReadBlock ready for the engine
     */
    private InputReadBlock buildInputBlock(String datasetName, Set<String> columnNames, List<String[]> rows) {
        String[] headers = columnNames.toArray(new String[0]);
        String[][] rawRows = rows.toArray(new String[0][]);
        return new InputReadBlock(rawRows, headers, datasetName);
    }
}
