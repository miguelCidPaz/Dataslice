package com.mcp.dataslice.adapters;

import com.mcp.dataslice.utils.DataSliceLogger;

/**
 * Factory class responsible for providing the appropriate DatasetAdapter
 * implementation based on the given file type.
 */
public class AdapterFactory {

    /**
     * Returns the appropriate DatasetAdapter implementation for the specified file type.
     * Supported types: csv, json, jsonl, parquet.
     * If the file type is unsupported, it logs a warning and returns null.
     *
     * @param filetype The file extension (e.g., "csv", "json").
     * @return A DatasetAdapter implementation or null if unsupported.
     */
    public static DatasetAdapter getAdapter(String filetype) {
        switch (filetype.toLowerCase()) {
            case "csv":
                return new CsvAdapter();
            case "json":
                return new JsonAdapter();
            case "jsonl":
                return new JsonlAdapter();
            case "parquet":
                return new ParquetAdapter();
            default:
                DataSliceLogger.warn("❌ Unsupported file type: " + filetype);
                return null;
        }
    }
}
