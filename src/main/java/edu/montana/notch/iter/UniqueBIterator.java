package edu.montana.notch.iter;

import java.util.HashSet;
import java.util.NoSuchElementException;

public class UniqueBIterator<T> implements BIterator<T> {
    protected final BIterator<T> iterator;

    public UniqueBIterator(BIterator<T> iterator) {
        this.iterator = iterator;
    }

    protected final HashSet<T> seen = new HashSet<>();
    protected T value;
    protected boolean hasValue;

    @Override
    public boolean hasNext() {
        while (true) {
            if (!iterator.hasNext()) return false;

            T val = iterator.next();
            if (!seen.add(val)) continue;

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
