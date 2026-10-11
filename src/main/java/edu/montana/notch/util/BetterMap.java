package edu.montana.notch.util;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BetterMap<K, V> extends AbstractMap<K, V> implements BetterIterable<Map.Entry<K, V>> {

    private final Map<K, V> delegate;
    private final Supplier<? extends V> onMiss;

    //=================================================================
    // Constructors, etc.
    //=================================================================
    public BetterMap() {
        this(new HashMap<>());
    }

    public BetterMap(Map<K, V> c) {
        this(c, null);
    }

    private BetterMap(Map<K, V> c, Supplier<? extends V> onMiss) {
        this.delegate = c;
        this.onMiss = onMiss;
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
    public V get(Object key) {
        if (onMiss == null || delegate.containsKey(key)) {
            return delegate.get(key);
        }
        return onMiss.get();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return delegate.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return delegate.putIfAbsent(key, value);
    }

    @Override
    public V replace(K key, V value) {
        return delegate.replace(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return delegate.replace(key, oldValue, newValue);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return delegate.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return delegate.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return delegate.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return delegate.merge(key, value, remappingFunction);
    }

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

    /**
     * Returns a view of this map where get() returns value for a missing key.
     * A miss does not change the map. Use only an immutable value, because
     * every miss returns the same object. For a new object on each miss, use
     * onMiss(Supplier).
     */
    public BetterMap<K, V> onMiss(V value) {
        return new BetterMap<>(delegate, () -> value);
    }

    /**
     * Returns a view of this map where get() calls supplier for a missing key.
     * A miss does not change the map. To store the value, use computeIfAbsent().
     */
    public BetterMap<K, V> onMiss(Supplier<? extends V> supplier) {
        return new BetterMap<>(delegate, supplier);
    }

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
