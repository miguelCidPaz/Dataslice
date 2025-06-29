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

        List<File> filesToProcess = new ArrayList<>();
        if (input.isDirectory()) {
            for (File f : input.listFiles()) if (f.isFile()) filesToProcess.add(f);
        } else if (input.isFile()) {
            filesToProcess.add(input);
        } else {
            DataSliceLogger.info("❌ Invalid path → " + path);
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

            
            for (ReadBatch batch : reader.read(file.getAbsolutePath(),
                    Constants.MAX_BLOCK_ROWS)) {
                pool.submit(() -> {
                    ProfilerRunner runner = new ProfilerRunner(threadCounter);
                    try {
						runner.runLearnMode(new InputReadBlock(
						        batch.getData(),
						        batch.getHeaders(),
						        batch.getDatasetName()));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                });
            }
        }

        
        pool.shutdown();
        try {
            pool.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        
        new ProfilerRunner(new AtomicInteger(1)).readProduct();
    }



    private static void printHelp() {
        DataSliceLogger.info("\n=== 📊 DataSlice CLI ===");
        DataSliceLogger.info("USAGE:");
        DataSliceLogger.info("  java -jar dataslice.jar <path to directory or file>");
        DataSliceLogger.info("EXAMPLE:");
        DataSliceLogger.info("  java -jar dataslice.jar data/");
    }
}
