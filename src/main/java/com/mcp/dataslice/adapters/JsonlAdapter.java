package com.mcp.dataslice.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp.dataslice.utils.DataSliceLogger;
import com.mcp.dataslice.utils.DatasetUtils;
import com.mcp.dataslice.models.InputReadBlock;

import java.io.*;
import java.util.*;

/**
 * Adapter for loading JSON Lines (JSONL) datasets efficiently.
 * Reads one JSON object per line, detects columns, batches rows, and builds InputReadBlocks.
 */
public class JsonlAdapter implements DatasetAdapter {

    public Iterable<InputReadBlock> loadDataset(String filePath, int batchSize) {
        List<InputReadBlock> blocks = new ArrayList<>();
        DataSliceLogger.info("🗂️ [JsonlAdapter] Streaming JSONL file: " + filePath);
        String datasetName = DatasetUtils.extractDatasetName(filePath);

        ObjectMapper mapper = new ObjectMapper();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            List<Map<String, String>> rawRows = new ArrayList<>();
            int totalRows = 0;
            Set<String> columnNames = new LinkedHashSet<>();

            String line;
            while ((line = br.readLine()) != null) {
                JsonNode node = mapper.readTree(line);

                Map<String, String> rowMap = new LinkedHashMap<>();
                node.fieldNames().forEachRemaining(field -> {
                    columnNames.add(field);
                    JsonNode value = node.get(field);
                    rowMap.put(field, (value != null && !value.isNull()) ? value.asText().trim() : "");
                });

                rawRows.add(rowMap);
                totalRows++;

                if (rawRows.size() >= batchSize) {
                    blocks.add(buildInputBlock(datasetName, columnNames, rawRows));
                    rawRows.clear();
                }
            }

            if (!rawRows.isEmpty()) {
                blocks.add(buildInputBlock(datasetName, columnNames, rawRows));
            }

            DataSliceLogger.info("✅ [JsonlAdapter] Total records streamed: " + totalRows);

        } catch (IOException e) {
            DataSliceLogger.error("❌ [JsonlAdapter] ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        return blocks;
    }

    private InputReadBlock buildInputBlock(String datasetName, Set<String> columnNames, List<Map<String, String>> rawRows) {
        String[] headers = columnNames.toArray(new String[0]);
        String[][] rows = new String[rawRows.size()][headers.length];

        for (int i = 0; i < rawRows.size(); i++) {
            Map<String, String> rowMap = rawRows.get(i);
            for (int j = 0; j < headers.length; j++) {
                rows[i][j] = rowMap.getOrDefault(headers[j], "");
            }
        }

        return new InputReadBlock(rows, headers, datasetName);
    }
}
