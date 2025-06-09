package com.mcp.dataslice.utils;

import java.util.Map;
import java.util.Set;

/**
 * Utility class providing common dataset-related operations and helpers.
 */
public class DatasetUtils {

    /**
     * Extracts the dataset name from the file path.
     *
     * @param filePath the full path to the file
     * @return the dataset name (file name only)
     */
    public static String extractDatasetName(String filePath) {
        String[] parts = filePath.replace("\\", "/").split("/");
        return parts[parts.length - 1];
    }

    /**
     * Checks if a string represents a numeric value.
     *
     * @param str the string to check
     * @return true if numeric, false otherwise
     */
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Limits an array to a maximum number of elements.
     *
     * @param array the original array
     * @param max the maximum number of elements
     * @return a trimmed array if longer, or the original if within limit
     */
    public static String[] limitArray(String[] array, int max) {
        if (array.length > max) {
            String[] limited = new String[max];
            System.arraycopy(array, 0, limited, 0, max);
            return limited;
        }
        return array;
    }

    /**
     * Infers the data type based on the current type and a new value.
     *
     * @param currentType the current type (e.g., STRING)
     * @param value the value to check
     * @return the inferred type
     */
    public static String inferType(String currentType, String value) {
        if ("STRING".equals(currentType) && isNumeric(value)) {
            return "NUMBER";
        }
        return currentType;
    }

    /**
     * Increments the null count for a column in a map.
     *
     * @param nullCountMap the map holding null counts per column
     * @param col the column name
     */
    public static void incrementNullCount(Map<String, Integer> nullCountMap, String col) {
        nullCountMap.put(col, nullCountMap.getOrDefault(col, 0) + 1);
    }

    /**
     * Builds a limited sample array from a set of sample values.
     *
     * @param samples the set of unique sample values
     * @param max the maximum number of samples
     * @return an array of limited samples
     */
    public static String[] buildLimitedSampleArray(Set<String> samples, int max) {
        String[] array = samples.toArray(new String[0]);
        return limitArray(array, max);
    }
}
