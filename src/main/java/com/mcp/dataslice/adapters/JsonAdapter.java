package com.mcp.dataslice.adapters;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp.dataslice.models.InputReadBlock;
import com.mcp.dataslice.utils.DataSliceLogger;
import com.mcp.dataslice.utils.DatasetUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Adapter for loading large JSON datasets efficiently using Jackson streaming.
 */
public class JsonAdapter implements DatasetAdapter {

    public Iterable<InputReadBlock> loadDataset(String filePath, int batchSize) {
        List<InputReadBlock> blocks = new ArrayList<>();
        DataSliceLogger.info("🗂️ [JsonAdapter] Streaming JSON file: " + filePath);
        String datasetName = DatasetUtils.extractDatasetName(filePath);

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = new JsonFactory();

        Set<String> columnNames = new LinkedHashSet<>();
        List<String[]> currentBlock = new ArrayList<>();
        int totalRows = 0;

        try (JsonParser parser = factory.createParser(new File(filePath))) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                DataSliceLogger.warn("❌ [JsonAdapter] JSON root is not an array.");
                return blocks;
            }

            while (parser.nextToken() == JsonToken.START_OBJECT) {
                JsonNode node = mapper.readTree(parser);

                // Recolectar columnas
                Iterator<String> fields = node.fieldNames();
                while (fields.hasNext()) {
                    columnNames.add(fields.next());
                }

                // Armar fila en el orden de columnas actual
                String[] row = new String[columnNames.size()];
                int i = 0;
                for (String col : columnNames) {
                    JsonNode valueNode = node.get(col);
                    row[i++] = (valueNode != null && !valueNode.isNull()) ? valueNode.asText().trim() : "";
                }

                currentBlock.add(row);
                totalRows++;

                if (currentBlock.size() >= batchSize) {
                    blocks.add(buildInputBlock(datasetName, columnNames, currentBlock));
                    currentBlock.clear();
                }
            }

            if (!currentBlock.isEmpty()) {
                blocks.add(buildInputBlock(datasetName, columnNames, currentBlock));
            }

            DataSliceLogger.info("✅ [JsonAdapter] Total records streamed: " + totalRows);

        } catch (IOException e) {
            DataSliceLogger.error("❌ [JsonAdapter] Streaming failed: " + e.getMessage());
            e.printStackTrace();
        }

        return blocks;
    }

    private InputReadBlock buildInputBlock(String datasetName, Set<String> columnNames, List<String[]> rows) {
        String[] headers = columnNames.toArray(new String[0]);
        String[][] rawRows = rows.toArray(new String[0][]);
        return new InputReadBlock(rawRows, headers, datasetName);
    }
}
