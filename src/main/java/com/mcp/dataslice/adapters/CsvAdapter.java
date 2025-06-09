package com.mcp.dataslice.adapters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import com.mcp.dataslice.utils.DataSliceLogger;
import com.mcp.dataslice.utils.DatasetUtils;
import com.mcp.dataslice.models.InputReadBlock;

/**
 * CsvAdapter is responsible for reading CSV files and converting them into batches
 * of InputReadBlock for further processing.
 */
public class CsvAdapter implements DatasetAdapter {

    private String[] headers;
    private String datasetName;

    /**
     * Loads the CSV dataset and returns an iterable over InputReadBlock batches.
     *
     * @param filePath  Path to the CSV file.
     * @param batchSize Number of rows per batch.
     * @return Iterable over InputReadBlock batches.
     */
    public Iterable<InputReadBlock> loadDataset(String filePath, int batchSize) {
        return new Iterable<InputReadBlock>() {
            @Override
            public Iterator<InputReadBlock> iterator() {
                return new CsvBatchIterator(filePath, batchSize);
            }
        };
    }

    /**
     * CsvBatchIterator handles reading CSV data in batches.
     */
    private class CsvBatchIterator implements Iterator<InputReadBlock> {
        private final BufferedReader reader;
        private final int batchSize;
        private boolean hasNext = true;

        public CsvBatchIterator(String filePath, int batchSize) {
            this.batchSize = batchSize;
            datasetName = DatasetUtils.extractDatasetName(filePath);
            BufferedReader tempReader = null;

            try {
                tempReader = new BufferedReader(new FileReader(filePath));
                String headerLine = tempReader.readLine();
                if (headerLine != null) {
                    headers = headerLine.split(",");
                    DataSliceLogger.info("[CsvAdapter] Detected headers: " + String.join(", ", headers));
                } else {
                    hasNext = false;
                    DataSliceLogger.error("[CsvAdapter] The file is empty or missing headers.");
                }
            } catch (IOException e) {
                hasNext = false;
                DataSliceLogger.error("[CsvAdapter] ERROR opening file: " + e.getMessage());
                e.printStackTrace();
            }

            this.reader = tempReader;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public InputReadBlock next() {
            try {
                var block = new ArrayList<String[]>();
                String line;
                int count = 0;

                while (count < batchSize && (line = reader.readLine()) != null) {
                    block.add(line.split(","));
                    count++;
                }

                if (block.isEmpty()) {
                    hasNext = false;
                    reader.close();
                    return null;
                }

                if (count < batchSize) {
                    hasNext = false;
                    reader.close();
                }

                String[][] rawRows = block.toArray(new String[0][]);
                return new InputReadBlock(rawRows, headers, datasetName);

            } catch (IOException e) {
                hasNext = false;
                DataSliceLogger.error("[CsvAdapter] ERROR reading block: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
    }

    public String[] getHeaders() {
        return headers;
    }
}
