package edu.montana.notch.iter;

public class UniqueBIterable<T> implements BetterIterable<T> {
    protected final BetterIterable<T> iterable;

    public UniqueBIterable(BetterIterable<T> iterable) {
        this.iterable = iterable;
    }

    @Override
    public BIterator<T> iterator() {
        return new UniqueBIterator<>(iterable.iterator());
    }
}
