package com.mcp.dataslice.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility logger class for DataSlice.
 * Wraps SLF4J logger and controls output based on the LOGS_ENABLED flag.
 */
public class DataSliceLogger {

    private static final Logger logger = LoggerFactory.getLogger("DatasliceLogger");

    /**
     * Logs an info-level message if logging is enabled.
     *
     * @param message the message to log
     */
    public static void info(String message) {
        if (Constants.LOGS_ENABLED) logger.info(message);
    }
    
    /**
     * Logs an info-level message if logging is enabled.
     *
     * @param message the message to log
     */
    public static void specialInfo(String message) {
        logger.info(message);
    }

    /**
     * Logs a warning-level message if logging is enabled.
     *
     * @param message the message to log
     */
    public static void warn(String message) {
        if (Constants.LOGS_ENABLED) logger.warn(message);
    }

    /**
     * Logs an error-level message if logging is enabled.
     *
     * @param message the message to log
     */
    public static void error(String message) {
        if (Constants.LOGS_ENABLED) logger.error(message);
    }
}
