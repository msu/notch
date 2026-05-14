package edu.montana.notch.util;

import java.util.*;
import java.util.function.Predicate;

public class BetterSet<T> extends AbstractSet<T> implements BetterIterable<T> {

    private final Set<T> delegate;

    //=================================================================
    // Constructors, etc.
    //=================================================================
    public BetterSet() {
        this(new LinkedHashSet<>());
    }

    public BetterSet(Collection<? extends T> c) {
        if(c instanceof Set s) {
            delegate = s;
        } else {
            delegate = new HashSet<>(c);
        }
    }

    public BetterSet(T[] arr) {
        this(Arrays.asList(arr));
    }

    public static <T> BetterSet<T> better(Collection<T> args) {
        return new BetterSet<>(args);
    }

    public static <T> BetterSet<T> insertionOrdered() {
        return new BetterSet<>(new LinkedHashSet<>());
    }

    //=================================================================
    // Set implementation
    //=================================================================

    @Override
    public Iterator<T> iterator() {
        return delegate.iterator();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean add(T t) {
        return delegate.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    //=================================================================
    // Extension methods
    //=================================================================

    public BetterSet<T> filterAsSet(Predicate<? super T> filter) {
        BetterSet<T> mappedResult = new BetterSet<>();
        for (T t : this) {
            if (filter.test(t)) {
                mappedResult.add(t);
            }
        }
        return mappedResult;
    }

    public BetterSet<T> copy() {
        return new BetterSet<>(new HashSet(delegate));
    }

    public BetterSet<T> union(BetterSet<T> other) {
        BetterSet<T> betterSet = new BetterSet<>();
        betterSet.addAll(this);
        betterSet.addAll(other);
        return betterSet;
    }

    public BetterSet<T> intersect(BetterSet<T> other) {
        BetterSet<T> betterSet = new BetterSet<>();
        for (T t : this) {
            if(other.contains(t)) {
                betterSet.add(t);
            }
        }
        return betterSet;
    }
}
