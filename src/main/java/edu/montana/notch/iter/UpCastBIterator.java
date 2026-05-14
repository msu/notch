package edu.montana.notch.iter;

import java.util.Iterator;

public class UpCastBIterator<T, R> implements BIterator<R> {
    protected final Iterator<T> iterator;
    protected final Class<R> clazz;

    public UpCastBIterator(Iterator<T> iterator, Class<R> clazz) {
        this.iterator = iterator;
        this.clazz = clazz;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public R next() {
        return clazz.cast(iterator.next());
    }
}
