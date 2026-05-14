package edu.montana.notch.iter;

import java.util.function.Function;

public class MapBIterable<T, R> implements BetterIterable<R> {
    protected final Iterable<T> iterable;
    protected final Function<T, R> mapper;

    public MapBIterable(Iterable<T> iterable, Function<T, R> mapper) {
        this.iterable = iterable;
        this.mapper = mapper;
    }

    @Override
    public BIterator<R> iterator() {
        return new MapBIterator<>(iterable.iterator(), mapper);
    }
}
