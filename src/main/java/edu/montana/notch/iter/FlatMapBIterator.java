package edu.montana.notch.iter;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class FlatMapBIterator<T> implements BIterator<T> {
    protected final Iterator<T> iterator;
    protected final Function<T, Iterable<T>> mapper;

    public FlatMapBIterator(Iterator<T> iterator, Function<T, Iterable<T>> mapper) {
        this.iterator = iterator;
        this.mapper = mapper;
    }

    private Iterator<T> internal;
    private boolean hasInternal = false;

    @Override
    public boolean hasNext() {
        boolean internalHasNext = internal.hasNext();
        if (!internalHasNext) hasInternal = false;

        if (!hasInternal) {
            if (!iterator.hasNext()) return false;
            T next = iterator.next();
            internal = mapper.apply(next).iterator();
            hasInternal = true;
        }

        return internalHasNext;
    }

    @Override
    public T next() {
        if (!hasInternal) throw new NoSuchElementException();
        return internal.next();
    }
}
