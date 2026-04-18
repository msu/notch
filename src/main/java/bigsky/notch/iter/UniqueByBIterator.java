package bigsky.notch.iter;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class UniqueByBIterator<T> implements BIterator<T> {
    protected final BIterator<T> iterator;
    protected final Function<T, Object> keyFn;

    public UniqueByBIterator(BIterator<T> iterator, Function<T, Object> keyFn) {
        this.iterator = iterator;
        this.keyFn = keyFn;
    }

    protected final HashSet<Object> seen = new HashSet<>();
    protected T value;
    protected boolean hasValue;

    @Override
    public boolean hasNext() {
        while (true) {
            if (!iterator.hasNext()) return false;

            T val = iterator.next();
            Object key = keyFn.apply(val);
            if (!seen.add(key)) continue;

            value = val;
            hasValue = true;
            break;
        }

        return true;
    }

    @Override
    public T next() {
        if (!hasValue) throw new NoSuchElementException();
        hasValue = false;
        return value;
    }
}
