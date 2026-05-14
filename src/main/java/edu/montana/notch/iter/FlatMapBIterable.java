package edu.montana.notch.iter;

import java.util.function.Function;

public class FlatMapBIterable<T> implements BetterIterable<T> {
    private final Iterable<T> iterable;
    private final Function<T, Iterable<T>> mapper;

    public FlatMapBIterable(Iterable<T> iterable, Function<T, Iterable<T>> mapper) {
        this.iterable = iterable;
        this.mapper = mapper;
    }

    @Override
    public BIterator<T> iterator() {
        return new FlatMapBIterator<>(iterable.iterator(), mapper);
    }
}
