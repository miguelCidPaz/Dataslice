package com.mcp.dataslice;

import com.mcp.dataslice.core.ProfilerRunner;
import com.mcp.dataslice.utils.Constants;
import com.mcp.dataslice.utils.DataSliceLogger;
import com.mcp.dataslice.models.InputReadBlock;

import com.mcp.io.models.ReadBatch;
import com.mcp.io.reader.TabularReader;
import com.mcp.io.factory.ReaderFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main entry point for the DataSlice CLI application.
 * Handles profiling over datasets provided as directories or individual files.
 */
public class Main {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        if (args.length < 1) {
            DataSliceLogger.info("❌ Error: Missing path to directory or file.");
            printHelp();
            return;
        }

        String path = args[0];
        handleLearn(path);

        long durationMillis = System.currentTimeMillis() - startTime;
        DataSliceLogger.specialInfo("⏱ Total time: " + durationMillis + " ms");
    }

    private static void handleLearn(String path) {
        File input = new File(path);
        int numThreads = Runtime.getRuntime().availableProcessors();
        ForkJoinPool pool = new ForkJoinPool(numThreads);
        AtomicInteger threadCounter = new AtomicInteger(1);

        DataSliceLogger.info("🛠 Pool configured with " + numThreads + " threads");

        List<File> filesToProcess = new ArrayList<>();

        if (input.isDirectory()) {
            File[] files = input.listFiles();
            if (files == null || files.length == 0) {
                DataSliceLogger.info("❌ Error: The directory is empty or unreadable.");
                return;
            }
            for (File file : files) {
                if (file.isFile()) filesToProcess.add(file);
            }
        } else if (input.isFile()) {
            filesToProcess.add(input);
        } else {
            DataSliceLogger.info("❌ Error: Invalid path → " + path);
            return;
        }

        for (File file : filesToProcess) {
            TabularReader reader;
            try {
                reader = ReaderFactory.of(file.getAbsolutePath());
            } catch (UnsupportedOperationException e) {
                DataSliceLogger.warn("❌ Unsupported file: " + file.getName());
                continue;
            }

            DataSliceLogger.info("📂 Reader loaded for: " + file.getName());

            ProfilerRunner runner = new ProfilerRunner(threadCounter);

            pool.submit(() -> {
                try {
                    Iterable<ReadBatch> batches = reader.read(file.getAbsolutePath(), Constants.MAX_BLOCK_ROWS);
                    for (ReadBatch batch : batches) {
                        InputReadBlock block = new InputReadBlock(
                            batch.getData(),
                            batch.getHeaders(),
                            batch.getDatasetName()
                        );
                        runner.runLearnMode(block);
                    }
                } catch (Exception e) {
                    DataSliceLogger.error("❌ Error processing " + file.getName() + ": " + e.getMessage());
                }
            });
        }

        try {
            pool.shutdown();
            if (!pool.awaitTermination(1, TimeUnit.HOURS)) {
                DataSliceLogger.warn("⚠ Timeout while waiting for threads to finish.");
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            DataSliceLogger.error("❌ Error waiting for threads → " + e.getMessage());
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        ProfilerRunner finalRunner = new ProfilerRunner(new AtomicInteger(1));
        finalRunner.readProduct();
    }

    private static void printHelp() {
        DataSliceLogger.info("\n=== 📊 DataSlice CLI ===");
        DataSliceLogger.info("USAGE:");
        DataSliceLogger.info("  java -jar dataslice.jar <path to directory or file>");
        DataSliceLogger.info("EXAMPLE:");
        DataSliceLogger.info("  java -jar dataslice.jar data/");
    }
}
