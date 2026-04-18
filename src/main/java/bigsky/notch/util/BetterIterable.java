package bigsky.notch.util;

import java.util.*;
import java.util.function.*;

public interface BetterIterable<T> extends Iterable<T> {

    //==============================================================================
    // Stream alternative (i hate streams)
    //==============================================================================

    default <Q> BetterList<Q> map(Function<T, Q> mapper) {
        BetterList<Q> mappedResult = new BetterList<>();
        for (T t : this) {
            mappedResult.add(mapper.apply(t));
        }
        return mappedResult;
    }

    default BetterIterable<T> tapEach(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        for (T t : this) {
            action.accept(t);
        }
        return this;
    }

    default Set<T> toSet() {
        LinkedHashSet<T> ts = new LinkedHashSet<>();
        forEach(ts::add);
        return ts;
    }

    default BetterList<T> toList() {
        BetterList<T> ts = new BetterList<>();
        forEach(ts::add);
        return ts;
    }

    default <K> Map<K, List<T>> toMap(Function<T, K> mapper) {
        Map<K, List<T>> mappedResult = new LinkedHashMap<>();
        for (T t : this) {
            mappedResult
                    .computeIfAbsent(mapper.apply(t), val -> new ArrayList<>())
                    .add(t);
        }
        return mappedResult;
    }

    default <K> TreeMap<K, List<T>> toOrderedMap(Function<T, K> mapper) {
        TreeMap<K, List<T>> mappedResult = new TreeMap<>();
        for (T t : this) {
            mappedResult
                    .computeIfAbsent(mapper.apply(t), val -> new ArrayList<>())
                    .add(t);
        }
        return mappedResult;
    }

    default <K> TreeMap<K, List<T>> toOrderedMap(Function<T, K> mapper, Comparator<? super K> comparator) {
        TreeMap<K, List<T>> mappedResult = new TreeMap<>(comparator);
        for (T t : this) {
            mappedResult
                    .computeIfAbsent(mapper.apply(t), val -> new ArrayList<>())
                    .add(t);
        }
        return mappedResult;
    }

    default <K> Map<K, T> toDistinctMap(Function<T, K> mapper) {
        Map<K, T> mappedResult = new HashMap<>();
        for (T t : this) {
            mappedResult.put(mapper.apply(t), t);
        }
        return mappedResult;
    }

    default <K> TreeMap<K, T> toOrderedDistinctMap(Function<T, K> mapper) {
        TreeMap<K, T> mappedResult = new TreeMap<>();
        for (T t : this) {
            mappedResult.put(mapper.apply(t), t);
        }
        return mappedResult;
    }

    default <K> TreeMap<K, T> toOrderedDistinctMap(Function<T, K> mapper, Comparator<? super K> comparator) {
        TreeMap<K, T> mappedResult = new TreeMap<>(comparator);
        for (T t : this) {
            mappedResult.put(mapper.apply(t), t);
        }
        return mappedResult;
    }

    default BetterList<T> filter(Predicate<? super T> filter) {
        BetterList<T> mappedResult = new BetterList<>();
        for (T t : this) {
            if (filter.test(t)) {
                mappedResult.add(t);
            }
        }
        return mappedResult;
    }

    default <U> BetterList<U> filterByType(Class<U> clazz) {
        BetterList<U> mappedResult = new BetterList<>();
        for (Object t : this) {
            if (clazz.isInstance(t)) {
                mappedResult.add(clazz.cast(t));
            }
        }
        return mappedResult;
    }

    default <U> BetterList<U> flatMap(Function<T, Iterable<U>> map) {
        BetterList<U> mappedResult = new BetterList<>();
        for (T t : this) {
            Iterable<U> apply = map.apply(t);
            for (U u : apply) {
                mappedResult.add(u);
            }
        }
        return mappedResult;
    }

    default String toString(String separator) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (T t : this) {
            if (i != 0) {
                builder.append(separator);
            }
            builder.append(t);
            i++;
        }
        return builder.toString();
    }

    default T first() {
        for (T t : this) {
            return t;
        }
        return null;
    }

    default T firstWhere(Predicate<? super T> predicate) {
        for (T t : this) {
            if (predicate.test(t)) {
                return t;
            }
        }
        return null;
    }

    default boolean hasMatch(Predicate<? super T> predicate) {
        for (T t : this) {
            if (predicate.test(t)) {
                return true;
            }
        }
        return false;
    }

    default boolean hasNoMatch(Predicate<? super T> predicate) {
        for (T t : this) {
            if (predicate.test(t)) {
                return false;
            }
        }
        return true;
    }

    default String join(String delimiter){
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Object o : this) {
            if (first) {
                first = false;
            } else {
                builder.append(delimiter);
            }
            builder.append(o);
        }
        return builder.toString();
    }

    static <T> BetterIterable<T> better(Iterable<T> iterable) {
        return iterable::iterator;
    }

}
