package edu.montana.notch.iter;

import java.util.Iterator;
import java.util.function.Function;

public class MapBIterator<T, R> implements BIterator<R> {
    protected final Iterator<T> iterator;
    protected final Function<T, R> mapper;

    public MapBIterator(Iterator<T> iterator, Function<T, R> mapper) {
        this.iterator = iterator;
        this.mapper = mapper;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public R next() {
        T value =  iterator.next();
        return mapper.apply(value);
    }
}
