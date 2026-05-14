package edu.montana.notch.iter;

public class SkipBIterable<T> implements BetterIterable<T> {
    protected final Iterable<T> iterable;
    protected final int n;

    public SkipBIterable(Iterable<T> iterable, int n) {
        this.iterable = iterable;
        this.n = n;
    }

    @Override
    public BIterator<T> iterator() {
        return new SkipBIterator<>(iterable.iterator(), n);
    }
}
