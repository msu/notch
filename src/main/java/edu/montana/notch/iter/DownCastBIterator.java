package edu.montana.notch.iter;

import java.util.Iterator;

public class DownCastBIterator<T, R extends T> implements BIterator<R> {
    protected final Iterator<T> iterator;
    protected final Class<R> clazz;

    public DownCastBIterator(Iterator<T> iterator, Class<R> clazz) {
        this.iterator = iterator;
        this.clazz = clazz;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public R next() {
        T value =  iterator.next();
        return clazz.cast(value);
    }
}
