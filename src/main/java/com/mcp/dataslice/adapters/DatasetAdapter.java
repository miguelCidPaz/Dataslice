package com.mcp.dataslice.adapters;

import com.mcp.dataslice.models.InputReadBlock;

/**
 * Interface for dataset adapters.
 * Defines the contract for loading datasets from different file types
 * and converting them into iterable batches of FlatBuffer byte arrays.
 */
public interface DatasetAdapter {

    /**
     * Loads the dataset from the given file path, splitting it into batches.
     *
     * @param filePath  the path to the dataset file
     * @param batchSize the maximum number of records per batch
     * @return an Iterable of byte arrays, each representing a batch in FlatBuffer format
     */
    Iterable<InputReadBlock> loadDataset(String filePath, int batchSize);
}
