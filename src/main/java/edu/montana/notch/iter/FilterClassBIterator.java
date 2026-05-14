package edu.montana.notch.iter;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class FilterClassBIterator<T, R extends T> implements BIterator<R> {
    protected final Iterator<T> iterator;
    protected final Class<R> clazz;

    public FilterClassBIterator(Iterator<T> iterator, Class<R> clazz) {
        this.iterator = iterator;
        this.clazz = clazz;
    }

    protected R value;
    protected boolean hasValue = false;

    @Override
    public boolean hasNext() {
        while (true) {
            if (!iterator.hasNext()) return false;

            T val = iterator.next();
            if (val == null) {
                value = null;
                hasValue = true;
                break;
            }

            if (clazz.isAssignableFrom(val.getClass())) {
                value = clazz.cast(val);
                hasValue = true;
                break;
            }
        }

        return true;
    }

    @Override
    public R next() {
        if (!hasValue) throw new NoSuchElementException();
        return value;
    }
}
