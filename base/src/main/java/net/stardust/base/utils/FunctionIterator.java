package net.stardust.base.utils;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

public class FunctionIterator<T, U> implements Iterator<U> {

    private Iterator<T> child;
    private Function<T, U> function;

    public FunctionIterator(Iterator<T> child, Function<T, U> function) {
        this.child = Objects.requireNonNull(child, "child");
        this.function = Objects.requireNonNull(function, "function");
    }

    @Override
    public boolean hasNext() {
        return child.hasNext();
    }

    @Override
    public U next() {
        return function.apply(child.next());
    }

    @Override
    public void remove() {
        child.remove();
    }

}
