package net.stardust.base.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Stream;

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

    public BatchList(int batchSize, Stream<? extends E> stream) {
        setBatchSize(batchSize);
        addAll(stream.collect(collector(batchSize)));
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
        if(totalBatches == 0 || size % batchSize != 0) {
            totalBatches++;
        }
        return totalBatches;
    }

    public int getBatchSize() {
        return batchSize;
    }

    private void setBatchSize(int batchSize) {
        if(batchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be greater than 0 but is " + batchSize);
        }
        this.batchSize = batchSize;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        if (o instanceof BatchList<?> list) {
            return batchSize == list.batchSize;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() * Integer.hashCode(batchSize);
    }

    @Override
    public String toString() {
        Iterator<E> iterator = iterator();
        if (!iterator.hasNext()) {
            return "(" + batchSize + ")[]";
        }
        int counter = 0;
        StringBuilder builder = new StringBuilder().append("(" + batchSize + ")[");
        while (true) {
            E element = iterator.next();
            if (counter % batchSize == 0) {
                if (counter != 0) {
                    builder.append("], ");
                }
                builder.append("[");
            }
            builder.append(element == this ? "(this Collection)" : element);
            if (!iterator.hasNext()) {
                builder.append("]");
                break;
            }
            if ((counter + 1) % batchSize != 0) {
                builder.append(", ");
            }
            counter++;
        }
        return builder.append("]").toString();
    }

    public static <E> BatchList<E> withBatchSize(int batchSize) {
        BatchList<E> batchList = new BatchList<>();
        batchList.setBatchSize(batchSize);
        return batchList;
    }

    public static <E> Collector<E, ?, BatchList<E>> collector(int batchSize) {
        Supplier<BatchList<E>> supplier = () -> withBatchSize(batchSize);
        BiConsumer<BatchList<E>, E> accumulator = BatchList::add;
        BinaryOperator<BatchList<E>> combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        Characteristics[] characteristics = {Characteristics.CONCURRENT, Characteristics.IDENTITY_FINISH};
        return Collector.of(supplier, accumulator, combiner, characteristics);
    }

}
