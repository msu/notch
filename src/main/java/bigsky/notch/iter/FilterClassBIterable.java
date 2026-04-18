package bigsky.notch.iter;

public class FilterClassBIterable<T, R extends T> implements BetterIterable<R> {
    protected final Iterable<T> iterable;
    protected final Class<R> clazz;

    public FilterClassBIterable(Iterable<T> iterable, Class<R> clazz) {
        this.iterable = iterable;
        this.clazz = clazz;
    }

    @Override
    public BIterator<R> iterator() {
        return new FilterClassBIterator<>(iterable.iterator(), clazz);
    }
}
