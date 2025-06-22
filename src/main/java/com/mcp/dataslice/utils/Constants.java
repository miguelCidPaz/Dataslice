package com.mcp.dataslice.utils;

/**
 * Constants class holding all global static configuration values
 * used across the DataSlice application.
 */
public final class Constants {

    /** Maximum number of rows per block for batch processing. */
    public static final int MAX_BLOCK_ROWS = 100000;
    
    public static final int PARTIAL_WRITE_THRESHOLD = 20;

    /** Flag to enable or disable logging globally. */
    public static final boolean LOGS_ENABLED = false;

    /** Private constructor to prevent instantiation. */
    private Constants() {}
}
