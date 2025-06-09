package com.mcp.dataslice.utils;

/**
 * Constants class holding all global static configuration values
 * used across the DataSlice application.
 */
public final class Constants {

    /** Estimated base number of columns to process (used for buffer sizing, etc.). */
    public static final int BASE_COLUMN_COUNT = 50;

    /** Maximum number of rows per block for batch processing. */
    public static final int MAX_BLOCK_ROWS = 100000;

    /** Maximum number of issues to track or report. */
    public static final int MAX_ISSUES = 1000;

    /** Maximum number of unique sample values to store per column. */
    public static final int MAX_SAMPLE_VALUES = 1;

    /** Number of batches to accumulate before performing a partial write. */
    public static final int PARTIAL_WRITE_THRESHOLD = 20;

    /** Flag to enable or disable logging globally. */
    public static final boolean LOGS_ENABLED = false;

    /** Private constructor to prevent instantiation. */
    private Constants() {}
}
