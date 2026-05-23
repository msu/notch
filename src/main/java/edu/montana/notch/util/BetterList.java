package edu.montana.notch.util;

import edu.montana.notch.iter.BIterator;
import edu.montana.notch.iter.BetterIterable;
import edu.montana.notch.iter.JavaBIterator;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BetterList<T> extends AbstractList<T> implements BetterIterable<T> {

    private final List<T> delegate;

    //=================================================================
    // Constructors, etc.
    //=================================================================
    public BetterList() {
        this(new ArrayList<>());
    }

    public BetterList(Collection<? extends T> c) {
        if (c instanceof List l) {
            delegate = l;
        } else {
            delegate = new ArrayList<>(c);
        }
    }

    public BetterList(T[] arr) {
        this(Arrays.asList(arr));
    }

    public static <T> BetterList<T> better(Collection<T> args) {
        return new BetterList<>(args);
    }

    //=================================================================
    // List implementation
    //=================================================================

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public T get(int index) {
        final int size = size();
        return delegate.get((size + index) % size);
    }

    ///  returns the value at `index` or returns the `defaultValue` if it is out of bounds
    public T get(int index, T defaultValue) {
        if (index >= 0 && index < delegate.size()) {
            return delegate.get(index);
        } else {
            return defaultValue;
        }
    }

    public T get(int index, Supplier<T> defaultValue) {
        if (index >= 0 && index < delegate.size()) {
            return delegate.get(index);
        } else {
            return defaultValue.get();
        }
    }

    public T getLast() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        } else {
            return get(size() - 1);
        }
    }

    @Override
    public T set(int index, T element) {
        return delegate.set(index, element);
    }

    @Override
    public boolean add(T t) {
        return delegate.add(t);
    }

    @Override
    public void add(int index, T element) {
        delegate.add(index, element);
    }

    @Override
    public T remove(int index) {
        return delegate.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    public T removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        } else {
            return delegate.remove(size() - 1);
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    //=================================================================
    // Extension methods
    //=================================================================

    @Override
    public T last() {
        return isEmpty() ? null : get(size() - 1);
    }

    @Override
    public BetterList<T> tap(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        for (T t : this) {
            action.accept(t);
        }
        return this;
    }

    public T lastWhere(Predicate<? super T> predicate) {
        for (int i = delegate.size() - 1; i >= 0; i--) {
            T t = delegate.get(i);
            if (predicate.test(t)) {
                return t;
            }
        }
        return null;
    }

    public BetterList<T> copy() {
        return new BetterList<>(new ArrayList<>(delegate));
    }

    public BetterList<T> concat(BetterList<T> other) {
        BetterList<T> ts = new BetterList<>();
        ts.addAll(this);
        ts.addAll(other);
        return ts;
    }
}
