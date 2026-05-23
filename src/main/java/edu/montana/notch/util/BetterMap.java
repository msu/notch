package edu.montana.notch.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class BetterMap<K, V> extends AbstractMap<K, V> implements BetterIterable<Map.Entry<K, V>> {

    private final Map<K, V> delegate;

    //=================================================================
    // Constructors, etc.
    //=================================================================
    public BetterMap() {
        this(new HashMap<>());
    }

    public BetterMap(Map<K, V> c) {
        delegate = c;
    }

    public static <K, V> BetterMap<K, V> better(Map<K, V> args) {
        return new BetterMap<>(args);
    }

    public static <K, V> BetterMap<K, V> of(Object... vals) {
        var map = new BetterMap();
        for (int i = 0; i < vals.length; i++) {
            Object key = vals[i];
            Object val = null;
            if (i++ < vals.length) {
                val = vals[i];
            }
            map.put(key, val);
        }
        return map;
    }

    public static <K, V> BetterMap<K, V> insertionOrdered() {
        return new BetterMap<>(new LinkedHashMap<>());
    }

    //=================================================================
    // Map implementation
    //=================================================================

    @Override
    public V put(K key, V value) {
        return delegate.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public BetterSet<Entry<K, V>> entrySet() {
        return new BetterSet<>(delegate.entrySet());
    }

    @Override
    public BetterSet<K> keySet() {
        return new BetterSet<>(super.keySet());
    }

    //=================================================================
    // Extension methods
    //=================================================================

    public BetterMap<K, V> filterAsMap(Predicate<? super Map.Entry<K, V>> filter) {
        BetterMap<K, V> mappedResult = new BetterMap<>();
        for (Map.Entry<K, V> t : this) {
            if (filter.test(t)) {
                mappedResult.put(t.getKey(), t.getValue());
            }
        }
        return mappedResult;
    }

    public BetterMap<K, V> filterByValues(Predicate<? super V> filter) {
        BetterMap<K, V> mappedResult = new BetterMap<>();
        for (Map.Entry<K, V> t : this) {
            if (filter.test(t.getValue())) {
                mappedResult.put(t.getKey(), t.getValue());
            }
        }
        return mappedResult;
    }

    public BetterMap<K, V> filterByKeys(Predicate<? super K> filter) {
        BetterMap<K, V> mappedResult = new BetterMap<>();
        for (Map.Entry<K, V> t : this) {
            if (filter.test(t.getKey())) {
                mappedResult.put(t.getKey(), t.getValue());
            }
        }
        return mappedResult;
    }

    public <Q> BetterMap<K, Q> mapValues(Function<V, Q> mapper) {
        BetterMap<K, Q> mappedResult = new BetterMap<>();
        for (Map.Entry<K, V> t : this) {
            mappedResult.put(t.getKey(), mapper.apply(t.getValue()));
        }
        return mappedResult;
    }

    public BetterMap<K, V> copy() {
        return new BetterMap<>(new HashMap<>(delegate));
    }

    public BetterMap<K, V> concat(BetterMap<K, V> other) {
        BetterMap<K, V> betterMap = new BetterMap<>();
        betterMap.putAll(this);
        betterMap.putAll(other);
        return betterMap;
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return delegate.entrySet().iterator();
    }
}
