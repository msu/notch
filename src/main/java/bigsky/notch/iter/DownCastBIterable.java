package bigsky.notch.iter;

public class DownCastBIterable<T, R extends T> implements BetterIterable<R> {
    protected final Iterable<T> iterable;
    protected final Class<R> clazz;

    public DownCastBIterable(Iterable<T> iterable, Class<R> clazz) {
        this.iterable = iterable;
        this.clazz = clazz;
    }

    @Override
    public BIterator<R> iterator() {
        return new DownCastBIterator<>(iterable.iterator(), clazz);
    }
}
