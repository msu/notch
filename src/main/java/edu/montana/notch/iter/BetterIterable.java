package edu.montana.notch.iter;

import edu.montana.notch.util.BetterList;
import edu.montana.notch.util.BetterMap;

import java.util.*;
import java.util.function.*;

public interface BetterIterable<T> extends Iterable<T> {
    @Override
    BIterator<T> iterator();

    default <R> BetterIterable<R> map(Function<T, R> mapper) {
        return new MapBIterable<>(this, mapper);
    }

    default BetterIterable<T> tap(Consumer<T> consumer) {
        return new TapBIterable<>(this, consumer);
    }

    default BetterIterable<T> filter(Predicate<T> filter) {
        return new FilterBIterable<>(this, filter);
    }

    default BetterIterable<T> filterOutNull() {
        return filter(Objects::nonNull);
    }

    default <R extends T> BetterIterable<R> filter(Class<R> clazz) {
        return new FilterClassBIterable<>(this, clazz);
    }

    default BetterIterable<T> skip(int n) {
        return new SkipBIterable<>(this, n);
    }

    default BetterIterable<T> take(int n) {
        return new TakeBIterable<>(this, n);
    }

    default <R extends T> BetterIterable<R> downcast(Class<R> clazz) {
        return new DownCastBIterable<>(this, clazz);
    }

    default <R> BetterIterable<R> upcast(Class<R> clazz) {
        // trust me bro (I wish java could do this)
        return new UpCastBIterable(this, clazz);
    }

    default BetterIterable<T> reverse() {
        return new ReverseBIterable<>(this);
    }

    default BetterIterable<T> unique() {
        return new UniqueBIterable<>(this);
    }

    default BetterIterable<T> distinct() {
        return unique();
    }

    default BetterIterable<T> uniqueBy(Function<T, Object> keyFn) {
        return new UniqueByBIterable<>(this, keyFn);
    }

    default BetterIterable<T> distinctBy(Function<T, Object> keyFn) {
        return uniqueBy(keyFn);
    }

    default BetterIterable<T> flatMap(Function<T, Iterable<T>> mapper) {
        return new FlatMapBIterable<>(this, mapper);
    }

    default BetterList<T> sort() {
        var list = toList();
        list.sort((o1, o2) -> ((Comparable) o1).compareTo(o2));
        return list;
    }

    default BetterList<T> sort(Comparator<T> comparator) {
        var list = toList();
        list.sort(comparator);
        return list;
    }

    default <R extends Comparable<R>> BetterList<T> sortBy(Function<T, R> keyFn) {
        return sort(Comparator.comparing(keyFn));
    }

    default <R> BetterList<T> sortBy(Function<T, R> keyFn, Comparator<R> comparator) {
        return sort(Comparator.comparing(keyFn, comparator));
    }

    default void forEach(Consumer<? super T> consumer) {
        iterator().forEachRemaining(consumer);
    }

    default BetterList<T> toList() {
        var out = new BetterList<T>();
        for (T t : this) {
            out.add(t);
        }
        return out;
    }

    default <K> BetterMap<K, T> toMap(Function<T, K> keyFn) {
        var map = new BetterMap<K, T>();
        for (T t : this) {
            map.put(keyFn.apply(t), t);
        }
        return map;
    }

    default <K extends Comparable<K>> BetterMap<K, BetterList<T>> toOrderedMap(Function<T, K> mapper) {
        TreeMap<K, BetterList<T>> mappedResult = new TreeMap<>();
        for (T t : this) {
            mappedResult
                    .computeIfAbsent(mapper.apply(t), val -> new BetterList<>())
                    .add(t);
        }
        return new BetterMap<>(mappedResult);
    }

    default <K> BetterMap<K, BetterList<T>> toOrderedMap(Function<T, K> mapper, Comparator<? super K> comparator) {
        TreeMap<K, BetterList<T>> mappedResult = new TreeMap<>(comparator);
        for (T t : this) {
            mappedResult
                    .computeIfAbsent(mapper.apply(t), val -> new BetterList<>())
                    .add(t);
        }
        return new BetterMap<>(mappedResult);
    }

    default <K> BetterMap<K, T> toDistinctMap(Function<T, K> mapper) {
        BetterMap<K, T> mappedResult = new BetterMap<>();
        for (T t : this) {
            mappedResult.put(mapper.apply(t), t);
        }
        return mappedResult;
    }

    default <K extends Comparable<K>> BetterMap<K, T> toOrderedDistinctMap(Function<T, K> mapper) {
        TreeMap<K, T> mappedResult = new TreeMap<>();
        for (T t : this) {
            mappedResult.put(mapper.apply(t), t);
        }
        return new BetterMap<>(mappedResult);
    }

    default <K> BetterMap<K, T> toOrderedDistinctMap(Function<T, K> mapper, Comparator<? super K> comparator) {
        TreeMap<K, T> mappedResult = new TreeMap<>(comparator);
        for (T t : this) {
            mappedResult.put(mapper.apply(t), t);
        }
        return new BetterMap<>(mappedResult);
    }

    default <R> BetterMap<T, R> mapTo(Function<T, R> valueFn) {
        var map = new BetterMap<T, R>();
        for (T t : this) {
            map.put(t, valueFn.apply(t));
        }
        return map;
    }

    default <R extends Comparable<R>> BetterMap<R, BetterList<T>> groupBy(Function<T, R> keyFn) {
        var map = new BetterMap<R, BetterList<T>>();
        for (T t : this) {
            R key = keyFn.apply(t);
            map.computeIfAbsent(key, k -> new BetterList<>()).add(t);
        }
        return map;
    }

    default <A> A fold(A initial, BiFunction<A, T, A> acc) {
        A out = initial;
        for (T v : this) {
            out = acc.apply(out, v);
        }
        return out;
    }

    default int count() {
        int count = 0;
        for (T ignored : this) count += 1;
        return count;
    }

    default T first() {
        for (T v : this) {
            return v;
        }
        throw new NoSuchElementException();
    }

    default T firstOrNull() {
        for (T v : this) {
            return v;
        }
        return null;
    }

    default T last() {
        var iterator = iterator();
        if (!iterator.hasNext()) throw new NoSuchElementException();
        T last;
        do {
            last = iterator.next();
        } while (iterator.hasNext());
        return last;
    }

    default T lastOrNull() {
        T last = null;
        for (T v : this) {
            last = v;
        }
        return last;
    }

    default T firstWhere(Predicate<T> predicate) {
        for (T v : this) {
            if (predicate.test(v)) {
                return v;
            }
        }
        throw new NoSuchElementException();
    }

    default T firstWhereOrNull(Predicate<T> predicate) {
        for (T v : this) {
            if (predicate.test(v)) return v;
        }
        return null;
    }

    default int pos(Predicate<T> predicate) {
        int i = 0;
        for (T v : this) {
            if (predicate.test(v)) {
                return i;
            }
            i += 1;
        }
        return -1;
    }

    default int indexOf(T value) {
        return pos(value::equals);
    }

    default boolean all(Predicate<T> predicate) {
        for (T v : this) {
            if (!predicate.test(v)) {
                return false;
            }
        }
        return true;
    }

    default boolean any(Predicate<T> predicate) {
        for (T v : this) {
            if (predicate.test(v)) return true;
        }
        return false;
    }

    default boolean none(Predicate<T> predicate) {
        for (T v : this) {
            if (predicate.test(v)) {
                return false;
            }
        }
        return true;
    }

    default String toString(String delim) {
        var out = new StringBuilder();
        int i = 0;
        for (T v : this) {
            if (i++ > 0) out.append(delim);
            out.append(v);
        }
        return out.toString();
    }

    default String toString(String prefix, String delim, String suffix) {
        return prefix + toString(delim) + suffix;
    }
}
