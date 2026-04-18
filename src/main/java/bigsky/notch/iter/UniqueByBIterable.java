package bigsky.notch.iter;

import java.util.function.Function;

public class UniqueByBIterable<T> implements BetterIterable<T> {
    protected final BetterIterable<T> iterable;
    protected final Function<T, Object> keyFn;

    public UniqueByBIterable(BetterIterable<T> iterable, Function<T, Object> keyFn) {
        this.iterable = iterable;
        this.keyFn = keyFn;
    }

    @Override
    public BIterator<T> iterator() {
        return new UniqueByBIterator<>(iterable.iterator(), keyFn);
    }
}
