package edu.montana.notch.iter;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class FilterBIterator<T> implements BIterator<T> {
    protected final Iterator<T> iterator;
    private final Predicate<T> filter;

    public FilterBIterator(Iterator<T> iterator, Predicate<T> filter) {
        this.iterator = iterator;
        this.filter = filter;
    }

    protected T value;
    protected boolean hasValue = false;

    @Override
    public boolean hasNext() {
        while (true) {
            if (!iterator.hasNext()) return false;

            T val = iterator.next();
            if (filter.test(val)) {
                value = val;
                hasValue = true;
                break;
            }
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
