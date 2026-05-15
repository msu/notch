package edu.montana.notch.util;

import java.util.*;
import java.util.function.*;

public interface BetterIterable<T> extends Iterable<T> {

    //==============================================================================
    // Transformations (eager, return BetterList)
    //==============================================================================

    default <R> BetterList<R> map(Function<T, R> mapper) {
        var out = new BetterList<R>();
        for (T t : this) {
            out.add(mapper.apply(t));
        }
        return out;
    }

    default <U> BetterList<U> flatMap(Function<T, Iterable<U>> mapper) {
        var out = new BetterList<U>();
        for (T t : this) {
            for (U u : mapper.apply(t)) {
                out.add(u);
            }
        }
        return out;
    }

    default BetterList<T> filter(Predicate<? super T> predicate) {
        var out = new BetterList<T>();
        for (T t : this) {
            if (predicate.test(t)) {
                out.add(t);
            }
        }
        return out;
    }

    default <U> BetterList<U> filter(Class<U> clazz) {
        var out = new BetterList<U>();
        for (T t : this) {
            if (clazz.isInstance(t)) {
                out.add(clazz.cast(t));
            }
        }
        return out;
    }

    default BetterList<T> filterOutNull() {
        return filter(Objects::nonNull);
    }

    default BetterList<T> skip(int n) {
        var out = new BetterList<T>();
        int i = 0;
        for (T t : this) {
            if (i++ >= n) out.add(t);
        }
        return out;
    }

    default BetterList<T> take(int n) {
        var out = new BetterList<T>();
        int i = 0;
        for (T t : this) {
            if (i++ >= n) break;
            out.add(t);
        }
        return out;
    }

    default BetterList<T> reverse() {
        var out = toList();
        Collections.reverse(out);
        return out;
    }

    default BetterList<T> unique() {
        return uniqueBy(t -> t);
    }

    default BetterList<T> uniqueBy(Function<T, Object> keyFn) {
        var seen = new HashSet<>();
        var out = new BetterList<T>();
        for (T t : this) {
            if (seen.add(keyFn.apply(t))) {
                out.add(t);
            }
        }
        return out;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default BetterList<T> sorted() {
        var out = toList();
        out.sort((a, b) -> ((Comparable) a).compareTo(b));
        return out;
    }

    default BetterList<T> sorted(Comparator<? super T> comparator) {
        var out = toList();
        out.sort(comparator);
        return out;
    }

    default <R extends Comparable<R>> BetterList<T> sortedBy(Function<T, R> keyFn) {
        return sorted(Comparator.comparing(keyFn));
    }

    default <R> BetterList<T> sortedBy(Function<T, R> keyFn, Comparator<? super R> comparator) {
        return sorted(Comparator.comparing(keyFn, comparator));
    }

    //==============================================================================
    // Side-effect (returns this)
    //==============================================================================

    default BetterIterable<T> tap(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        for (T t : this) {
            action.accept(t);
        }
        return this;
    }

    //==============================================================================
    // Reductions to a single value
    //==============================================================================

    default T first() {
        for (T t : this) {
            return t;
        }
        return null;
    }

    default T firstOrThrow() {
        for (T t : this) {
            return t;
        }
        throw new NoSuchElementException();
    }

    default T last() {
        T last = null;
        for (T t : this) {
            last = t;
        }
        return last;
    }

    default T lastOrThrow() {
        var iterator = iterator();
        if (!iterator.hasNext()) throw new NoSuchElementException();
        T last;
        do {
            last = iterator.next();
        } while (iterator.hasNext());
        return last;
    }

    default T firstWhere(Predicate<? super T> predicate) {
        for (T t : this) {
            if (predicate.test(t)) return t;
        }
        return null;
    }

    default T firstWhereOrThrow(Predicate<? super T> predicate) {
        for (T t : this) {
            if (predicate.test(t)) return t;
        }
        throw new NoSuchElementException();
    }

    default int indexOf(T value) {
        return indexOf(t -> Objects.equals(t, value));
    }

    default int indexOf(Predicate<? super T> predicate) {
        int i = 0;
        for (T t : this) {
            if (predicate.test(t)) return i;
            i++;
        }
        return -1;
    }

    default int count() {
        int n = 0;
        for (T ignored : this) n++;
        return n;
    }

    default <A> A fold(A initial, BiFunction<A, T, A> acc) {
        A out = initial;
        for (T t : this) {
            out = acc.apply(out, t);
        }
        return out;
    }

    //==============================================================================
    // Booleans
    //==============================================================================

    default boolean hasMatch(Predicate<? super T> predicate) {
        for (T t : this) {
            if (predicate.test(t)) return true;
        }
        return false;
    }

    default boolean hasNoMatch(Predicate<? super T> predicate) {
        for (T t : this) {
            if (predicate.test(t)) return false;
        }
        return true;
    }

    default boolean all(Predicate<? super T> predicate) {
        for (T t : this) {
            if (!predicate.test(t)) return false;
        }
        return true;
    }

    //==============================================================================
    // Collect to collection
    //==============================================================================

    default BetterList<T> toList() {
        var out = new BetterList<T>();
        for (T t : this) {
            out.add(t);
        }
        return out;
    }

    default BetterSet<T> toSet() {
        var out = new BetterSet<T>(new LinkedHashSet<>());
        for (T t : this) {
            out.add(t);
        }
        return out;
    }

    //==============================================================================
    // Collect to map
    //==============================================================================

    default <K> BetterMap<K, T> toMap(Function<T, K> keyFn) {
        var map = new BetterMap<K, T>();
        for (T t : this) {
            map.put(keyFn.apply(t), t);
        }
        return map;
    }

    default <K> BetterMap<K, T> toMap(Function<T, K> keyFn, Comparator<? super K> comparator) {
        var tree = new TreeMap<K, T>(comparator);
        for (T t : this) {
            tree.put(keyFn.apply(t), t);
        }
        return new BetterMap<>(tree);
    }

    default <K, V> BetterMap<K, V> toMap(Function<T, K> keyFn, Function<T, V> valueFn) {
        var map = new BetterMap<K, V>();
        for (T t : this) {
            map.put(keyFn.apply(t), valueFn.apply(t));
        }
        return map;
    }

    default <K> BetterMap<K, BetterList<T>> groupBy(Function<T, K> keyFn) {
        var map = new BetterMap<K, BetterList<T>>();
        for (T t : this) {
            map.computeIfAbsent(keyFn.apply(t), k -> new BetterList<>()).add(t);
        }
        return map;
    }

    default <K> BetterMap<K, BetterList<T>> groupBy(Function<T, K> keyFn, Comparator<? super K> comparator) {
        var tree = new TreeMap<K, BetterList<T>>(comparator);
        for (T t : this) {
            tree.computeIfAbsent(keyFn.apply(t), k -> new BetterList<>()).add(t);
        }
        return new BetterMap<>(tree);
    }

    default <R> BetterMap<T, R> asMap(Function<T, R> valueFn) {
        var map = new BetterMap<T, R>();
        for (T t : this) {
            map.put(t, valueFn.apply(t));
        }
        return map;
    }

    //==============================================================================
    // Strings
    //==============================================================================

    default String toString(String delim) {
        var out = new StringBuilder();
        int i = 0;
        for (T t : this) {
            if (i++ > 0) out.append(delim);
            out.append(t);
        }
        return out.toString();
    }

    default String toString(String prefix, String delim, String suffix) {
        return prefix + toString(delim) + suffix;
    }

    //==============================================================================
    // Static factory
    //==============================================================================

    static <T> BetterIterable<T> better(Iterable<T> iterable) {
        return iterable::iterator;
    }
}