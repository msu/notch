package bigsky.notch.util;

import bigsky.notch.iter.BetterIterable;
import bigsky.notch.iter.JavaBIterable;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BIterableTest {

    private BetterIterable<Integer> createIterable(Integer... values) {
        return new JavaBIterable<>(Arrays.asList(values));
    }

    private BetterIterable<String> createStringIterable(String... values) {
        return new JavaBIterable<>(Arrays.asList(values));
    }

    // ========== map ==========
    @Test
    void testMap() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5);

        BetterList<String> mapped = numbers.map(String::valueOf).toList();
        assertEquals(Arrays.asList("1", "2", "3", "4", "5"), mapped);

        BetterList<Integer> doubled = numbers.map(n -> n * 2).toList();
        assertEquals(Arrays.asList(2, 4, 6, 8, 10), doubled);
    }

    @Test
    void testMapEmpty() {
        BetterIterable<Integer> empty = createIterable();
        BetterList<String> mapped = empty.map(String::valueOf).toList();
        assertEquals(0, mapped.size());
    }

    // ========== inspect ==========
    @Test
    void testTap() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3);
        List<Integer> inspected = new ArrayList<>();

        BetterList<Integer> result = numbers.tap(inspected::add).toList();

        assertEquals(Arrays.asList(1, 2, 3), result);
        assertEquals(Arrays.asList(1, 2, 3), inspected);
    }

    @Test
    void testTapEmpty() {
        BetterIterable<Integer> empty = createIterable();
        List<Integer> inspected = new ArrayList<>();

        BetterList<Integer> result = empty.tap(inspected::add).toList();

        assertEquals(0, result.size());
        assertEquals(0, inspected.size());
    }

    // ========== filter ==========
    @Test
    void testFilter() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        BetterList<Integer> evenNumbers = numbers.filter(n -> n % 2 == 0).toList();
        assertEquals(Arrays.asList(2, 4, 6, 8, 10), evenNumbers);

        BetterList<Integer> greaterThanFive = numbers.filter(n -> n > 5).toList();
        assertEquals(Arrays.asList(6, 7, 8, 9, 10), greaterThanFive);

        BetterList<Integer> noMatches = numbers.filter(n -> n > 20).toList();
        assertEquals(0, noMatches.size());
    }

    @Test
    void testFilterOutNull() {
        BetterIterable<String> withNulls = createStringIterable("hello", null, "world", null, "test");
        BetterList<String> filtered = withNulls.filterOutNull().toList();
        assertEquals(Arrays.asList("hello", "world", "test"), filtered);
    }

    @Test
    void testFilterByClass() {
        List<Object> mixedObjects = Arrays.asList("hello", 42, "world", 3.14, 100, "test");
        BetterIterable<Object> betterMixed = new JavaBIterable<>(mixedObjects);

        BetterList<String> strings = betterMixed.filter(String.class).toList();
        assertEquals(Arrays.asList("hello", "world", "test"), strings);

        BetterList<Integer> integers = betterMixed.filter(Integer.class).toList();
        assertEquals(Arrays.asList(42, 100), integers);
    }

    // ========== skip and take ==========
    @Test
    void testSkip() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5);

        BetterList<Integer> skipped = numbers.skip(2).toList();
        assertEquals(Arrays.asList(3, 4, 5), skipped);

        BetterList<Integer> skipAll = numbers.skip(10).toList();
        assertEquals(0, skipAll.size());

        BetterList<Integer> skipNone = numbers.skip(0).toList();
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), skipNone);
    }

    @Test
    void testTake() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5);

        BetterList<Integer> taken = numbers.take(3).toList();
        assertEquals(Arrays.asList(1, 2, 3), taken);

        BetterList<Integer> takeMore = numbers.take(10).toList();
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), takeMore);

        BetterList<Integer> takeNone = numbers.take(0).toList();
        assertEquals(0, takeNone.size());
    }

    @Test
    void testSkipAndTake() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        BetterList<Integer> result = numbers.skip(2).take(5).toList();
        assertEquals(Arrays.asList(3, 4, 5, 6, 7), result);
    }

    // ========== reverse ==========
    @Test
    void testReverse() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5);
        BetterList<Integer> reversed = numbers.reverse().toList();
        assertEquals(Arrays.asList(5, 4, 3, 2, 1), reversed);
    }

    @Test
    void testReverseEmpty() {
        BetterIterable<Integer> empty = createIterable();
        BetterList<Integer> reversed = empty.reverse().toList();
        assertEquals(0, reversed.size());
    }

    @Test
    void testReverseSingle() {
        BetterIterable<Integer> single = createIterable(42);
        BetterList<Integer> reversed = single.reverse().toList();
        assertEquals(Arrays.asList(42), reversed);
    }

    // ========== unique and distinct ==========
    @Test
    void testUnique() {
        BetterIterable<Integer> withDuplicates = createIterable(1, 2, 2, 3, 1, 4, 3, 5);
        BetterList<Integer> unique = withDuplicates.unique().toList();
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), unique);
    }

    @Test
    void testDistinct() {
        BetterIterable<Integer> withDuplicates = createIterable(1, 2, 2, 3, 1, 4, 3, 5);
        BetterList<Integer> distinct = withDuplicates.distinct().toList();
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), distinct);
    }

    @Test
    void testUniqueBy() {
        BetterIterable<String> words = createStringIterable("cat", "car", "dog", "door", "cab");
        BetterList<String> uniqueByFirstChar = words.uniqueBy(s -> s.charAt(0)).toList();
        assertEquals(Arrays.asList("cat", "dog"), uniqueByFirstChar);
    }

    @Test
    void testUniqueByLength() {
        BetterIterable<String> words = createStringIterable("a", "bb", "ccc", "d", "ee", "fff");
        BetterList<String> uniqueByLength = words.uniqueBy(String::length).toList();
        assertEquals(Arrays.asList("a", "bb", "ccc"), uniqueByLength);
    }

    @Test
    void testDistinctBy() {
        BetterIterable<String> words = createStringIterable("cat", "car", "dog", "door", "cab");
        BetterList<String> distinctByFirstChar = words.distinctBy(s -> s.charAt(0)).toList();
        assertEquals(Arrays.asList("cat", "dog"), distinctByFirstChar);
    }

    // ========== sort ==========
    @Test
    void testSort() {
        BetterIterable<Integer> numbers = createIterable(5, 2, 8, 1, 9, 3);
        BetterList<Integer> sorted = numbers.sort();
        assertEquals(Arrays.asList(1, 2, 3, 5, 8, 9), sorted);
    }

    @Test
    void testSortStrings() {
        BetterIterable<String> words = createStringIterable("zebra", "apple", "banana", "cherry");
        BetterList<String> sorted = words.sort();
        assertEquals(Arrays.asList("apple", "banana", "cherry", "zebra"), sorted);
    }

    @Test
    void testSortWithComparator() {
        BetterIterable<Integer> numbers = createIterable(5, 2, 8, 1, 9, 3);
        BetterList<Integer> sortedDesc = numbers.sort(Comparator.reverseOrder());
        assertEquals(Arrays.asList(9, 8, 5, 3, 2, 1), sortedDesc);
    }

    @Test
    void testSortEmpty() {
        BetterIterable<Integer> empty = createIterable();
        BetterList<Integer> sorted = empty.sort();
        assertEquals(0, sorted.size());
    }

    @Test
    void testSortBy() {
        BetterIterable<String> words = createStringIterable("zzz", "a", "bb", "cccc");
        BetterList<String> sortedByLength = words.sortBy(String::length);
        assertEquals(Arrays.asList("a", "bb", "zzz", "cccc"), sortedByLength);
    }

    @Test
    void testSortByWithComparator() {
        BetterIterable<String> words = createStringIterable("zzz", "a", "bb", "cccc");
        BetterList<String> sortedByLengthDesc = words.sortBy(String::length, Comparator.reverseOrder());
        assertEquals(Arrays.asList("cccc", "zzz", "bb", "a"), sortedByLengthDesc);
    }

    // ========== toMap ==========
    @Test
    void testToMap() {
        BetterIterable<String> words = createStringIterable("cat", "dog", "car");
        BetterMap<Character, String> map = words.toMap(s -> s.charAt(0));

        assertEquals(2, map.size());
        // Last value for 'c' should win
        assertEquals("car", map.get('c'));
        assertEquals("dog", map.get('d'));
    }

    @Test
    void testToMapEmpty() {
        BetterIterable<String> empty = createStringIterable();
        BetterMap<Character, String> map = empty.toMap(s -> s.charAt(0));
        assertEquals(0, map.size());
    }

    // ========== mapTo ==========
    @Test
    void testMapTo() {
        BetterIterable<String> words = createStringIterable("cat", "dog", "bird");
        BetterMap<String, Integer> map = words.mapTo(String::length);

        assertEquals(3, map.size());
        assertEquals(3, map.get("cat"));
        assertEquals(3, map.get("dog"));
        assertEquals(4, map.get("bird"));
    }

    @Test
    void testMapToEmpty() {
        BetterIterable<String> empty = createStringIterable();
        BetterMap<String, Integer> map = empty.mapTo(String::length);
        assertEquals(0, map.size());
    }

    // ========== groupBy ==========
    @Test
    void testGroupBy() {
        BetterIterable<String> words = createStringIterable("cat", "car", "dog", "door", "cab");
        BetterMap<Character, BetterList<String>> grouped = words.groupBy(s -> s.charAt(0));

        assertEquals(2, grouped.size());
        assertEquals(Arrays.asList("cat", "car", "cab"), grouped.get('c'));
        assertEquals(Arrays.asList("dog", "door"), grouped.get('d'));
    }

    @Test
    void testGroupByLength() {
        BetterIterable<String> words = createStringIterable("a", "bb", "cc", "ddd", "e");
        BetterMap<Integer, BetterList<String>> grouped = words.groupBy(String::length);

        assertEquals(3, grouped.size());
        assertEquals(Arrays.asList("a", "e"), grouped.get(1));
        assertEquals(Arrays.asList("bb", "cc"), grouped.get(2));
        assertEquals(Arrays.asList("ddd"), grouped.get(3));
    }

    @Test
    void testGroupByEmpty() {
        BetterIterable<String> empty = createStringIterable();
        BetterMap<Character, BetterList<String>> grouped = empty.groupBy(s -> s.charAt(0));
        assertEquals(0, grouped.size());
    }

    // ========== fold ==========
    @Test
    void testFold() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5);
        Integer sum = numbers.fold(0, Integer::sum);
        assertEquals(15, sum);
    }

    @Test
    void testFoldProduct() {
        BetterIterable<Integer> numbers = createIterable(2, 3, 4);
        Integer product = numbers.fold(1, (a, b) -> a * b);
        assertEquals(24, product);
    }

    @Test
    void testFoldConcat() {
        BetterIterable<String> words = createStringIterable("hello", " ", "world");
        String result = words.fold("", (acc, s) -> acc + s);
        assertEquals("hello world", result);
    }

    @Test
    void testFoldEmpty() {
        BetterIterable<Integer> empty = createIterable();
        Integer sum = empty.fold(100, Integer::sum);
        assertEquals(100, sum);
    }

    // ========== count ==========
    @Test
    void testCount() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5);
        assertEquals(5, numbers.count());
    }

    @Test
    void testCountEmpty() {
        BetterIterable<Integer> empty = createIterable();
        assertEquals(0, empty.count());
    }

    @Test
    void testCountAfterFilter() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        assertEquals(5, numbers.filter(n -> n % 2 == 0).count());
    }

    // ========== first and last ==========
    @Test
    void testFirst() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5);
        assertEquals(1, numbers.first());
    }

    @Test
    void testFirstThrowsOnEmpty() {
        BetterIterable<Integer> empty = createIterable();
        assertThrows(NoSuchElementException.class, empty::first);
    }

    @Test
    void testFirstOrNull() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5);
        assertEquals(1, numbers.firstOrNull());
    }

    @Test
    void testFirstOrNullEmpty() {
        BetterIterable<Integer> empty = createIterable();
        assertNull(empty.firstOrNull());
    }

    @Test
    void testLast() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5);
        assertEquals(5, numbers.last());
    }

    @Test
    void testLastThrowsOnEmpty() {
        BetterIterable<Integer> empty = createIterable();
        assertThrows(NoSuchElementException.class, empty::last);
    }

    @Test
    void testLastOrNull() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5);
        assertEquals(5, numbers.lastOrNull());
    }

    @Test
    void testLastOrNullEmpty() {
        BetterIterable<Integer> empty = createIterable();
        assertNull(empty.lastOrNull());
    }

    // ========== all, any, none ==========
    @Test
    void testAll() {
        BetterIterable<Integer> numbers = createIterable(2, 4, 6, 8, 10);
        assertTrue(numbers.all(n -> n % 2 == 0));
        assertFalse(numbers.all(n -> n > 5));
    }

    @Test
    void testAllEmpty() {
        BetterIterable<Integer> empty = createIterable();
        assertTrue(empty.all(n -> false)); // vacuous truth
    }

    @Test
    void testAny() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5);
        assertTrue(numbers.any(n -> n % 2 == 0));
        assertTrue(numbers.any(n -> n > 4));
        assertFalse(numbers.any(n -> n > 10));
    }

    @Test
    void testAnyEmpty() {
        BetterIterable<Integer> empty = createIterable();
        assertFalse(empty.any(n -> true));
    }

    @Test
    void testNone() {
        BetterIterable<Integer> numbers = createIterable(1, 3, 5, 7, 9);
        assertTrue(numbers.none(n -> n % 2 == 0));
        assertFalse(numbers.none(n -> n > 5));
    }

    @Test
    void testNoneEmpty() {
        BetterIterable<Integer> empty = createIterable();
        assertTrue(empty.none(n -> true)); // vacuous truth
    }

    // ========== chained operations ==========
    @Test
    void testChainedOperations() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        BetterList<Integer> result = numbers
                .filter(n -> n % 2 == 0)
                .map(n -> n * 2)
                .take(3)
                .toList();

        assertEquals(Arrays.asList(4, 8, 12), result);
    }

    @Test
    void testComplexChainedOperations() {
        BetterIterable<String> words = createStringIterable("apple", "banana", "apricot", "cherry", "avocado", "blueberry");

        BetterList<String> result = words
                .filter(s -> s.startsWith("a") || s.startsWith("b"))
                .map(String::toUpperCase)
                .unique()
                .sort()
                .toList();

        assertEquals(Arrays.asList("APPLE", "APRICOT", "AVOCADO", "BANANA", "BLUEBERRY"), result);
    }

    @Test
    void testChainedWithGroupBy() {
        BetterIterable<String> words = createStringIterable("cat", "car", "dog", "door", "cab", "ant");

        BetterMap<Character, BetterList<String>> result = words
                .filter(s -> s.length() <= 3)
                .groupBy(s -> s.charAt(0));

        assertEquals(3, result.size());
        assertEquals(Arrays.asList("cat", "car", "cab"), result.get('c'));
        assertEquals(Arrays.asList("dog"), result.get('d'));
        assertEquals(Arrays.asList("ant"), result.get('a'));
    }

    // ========== toList ==========
    @Test
    void testToList() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5);
        BetterList<Integer> list = numbers.toList();

        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
        assertTrue(list instanceof BetterList);
    }

    @Test
    void testToListEmpty() {
        BetterIterable<Integer> empty = createIterable();
        BetterList<Integer> list = empty.toList();
        assertEquals(0, list.size());
    }

    // ========== forEach ==========
    @Test
    void testForEach() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5);
        List<Integer> collected = new ArrayList<>();

        numbers.forEach(collected::add);

        assertEquals(Arrays.asList(1, 2, 3, 4, 5), collected);
    }

    @Test
    void testForEachEmpty() {
        BetterIterable<Integer> empty = createIterable();
        List<Integer> collected = new ArrayList<>();

        empty.forEach(collected::add);

        assertEquals(0, collected.size());
    }
}
