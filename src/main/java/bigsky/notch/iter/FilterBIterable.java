package bigsky.notch.iter;

import java.util.function.Predicate;

public class FilterBIterable<T> implements BetterIterable<T> {
    protected final Iterable<T> iterable;
    protected final Predicate<T> filter;

    public FilterBIterable(Iterable<T> iterable, Predicate<T> filter) {
        this.iterable = iterable;
        this.filter = filter;
    }

    @Override
    public BIterator<T> iterator() {
        return new FilterBIterator<>(iterable.iterator(), filter);
    }
}
