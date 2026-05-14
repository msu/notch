package edu.montana.notch.iter;

public class UpCastBIterable<T extends R, R> implements BetterIterable<R> {
    protected final Iterable<T> iterable;
    protected final Class<R> clazz;

    public UpCastBIterable(Iterable<T> iterable, Class<R> clazz) {
        this.iterable = iterable;
        this.clazz = clazz;
    }

    @Override
    public BIterator<R> iterator() {
        return new UpCastBIterator<>(iterable.iterator(), clazz);
    }
}
