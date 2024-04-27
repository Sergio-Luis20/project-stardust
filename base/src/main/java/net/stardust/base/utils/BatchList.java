package net.stardust.base.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;

public class BatchList<E> extends ArrayList<E> {
    
    private int batchSize;

    private BatchList() {
        super();
    }

    public BatchList(int batchSize, Collection<? extends E> elements) {
        super(elements);
        setBatchSize(batchSize);
    }

    public BatchList(int batchSize, int initialCapacity) {
        super(initialCapacity);
        setBatchSize(batchSize);
    }

    public List<E> getBatch(int index) {
        int totalBatches = getTotalBatches();
        if(index < 0 || index >= totalBatches) {
            throw new IndexOutOfBoundsException(index);
        }
        int size = size();
        int startIndex = index * batchSize;
        int endIndex = startIndex + batchSize;
        if(endIndex > size) {
            endIndex = size;
        }
        return new ArrayList<>(subList(startIndex, endIndex));
    }

    public List<List<E>> getBatches() {
        List<List<E>> batches = new ArrayList<>();
        int index = 0;
        int size = size();
        while(index < size) {
            int endIndex = index + batchSize;
            if(endIndex > size) {
                endIndex = size;
            }
            List<E> subList = subList(index, endIndex);
            batches.add(new ArrayList<>(subList));
            index += batchSize;
        }
        return batches;
    }

    public int getTotalBatches() {
        int size = size();
        if(size == 0) {
            return 0;
        }
        int totalBatches = size / batchSize;
        if(totalBatches == 0 || size % totalBatches != 0) {
            totalBatches++;
        }
        return totalBatches;
    }

    public int getBatchSize() {
        return batchSize;
    }

    private void setBatchSize(int batchSize) {
        if(batchSize < 0) {
            throw new IllegalArgumentException("negative batch size");
        }
        this.batchSize = batchSize;
    }

    public static <E> BatchList<E> withBatchSize(int batchSize) {
        BatchList<E> batchList = new BatchList<>();
        batchList.setBatchSize(batchSize);
        return batchList;
    }

    public static <E> Collector<E, ?, BatchList<E>> collector(int batchSize) {
        return Collector.of(() -> withBatchSize(batchSize), List::add, (left, right) -> {
            left.addAll(right);
            return left;
        }, Characteristics.IDENTITY_FINISH);
    }

}
