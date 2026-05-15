package edu.montana.notch.util;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BIterableTest {

    private BetterIterable<Integer> createIterable(Integer... values) {
        return BetterIterable.better(Arrays.asList(values));
    }

    private BetterIterable<String> createStringIterable(String... values) {
        return BetterIterable.better(Arrays.asList(values));
    }

    // ========== map ==========
    @Test
    void testMap() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5);

        BetterList<String> mapped = numbers.map(String::valueOf);
        assertEquals(Arrays.asList("1", "2", "3", "4", "5"), mapped);

        BetterList<Integer> doubled = numbers.map(n -> n * 2);
        assertEquals(Arrays.asList(2, 4, 6, 8, 10), doubled);
    }

    @Test
    void testMapEmpty() {
        BetterIterable<Integer> empty = createIterable();
        BetterList<String> mapped = empty.map(String::valueOf);
        assertEquals(0, mapped.size());
    }

    // ========== tap ==========
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

        BetterList<Integer> evenNumbers = numbers.filter(n -> n % 2 == 0);
        assertEquals(Arrays.asList(2, 4, 6, 8, 10), evenNumbers);

        BetterList<Integer> greaterThanFive = numbers.filter(n -> n > 5);
        assertEquals(Arrays.asList(6, 7, 8, 9, 10), greaterThanFive);

        BetterList<Integer> noMatches = numbers.filter(n -> n > 20);
        assertEquals(0, noMatches.size());
    }

    @Test
    void testFilterOutNull() {
        BetterIterable<String> withNulls = createStringIterable("hello", null, "world", null, "test");
        BetterList<String> filtered = withNulls.filterOutNull();
        assertEquals(Arrays.asList("hello", "world", "test"), filtered);
    }

    @Test
    void testFilterByClass() {
        List<Object> mixedObjects = Arrays.asList("hello", 42, "world", 3.14, 100, "test");
        BetterIterable<Object> betterMixed = BetterIterable.better(mixedObjects);

        BetterList<String> strings = betterMixed.filter(String.class);
        assertEquals(Arrays.asList("hello", "world", "test"), strings);

        BetterList<Integer> integers = betterMixed.filter(Integer.class);
        assertEquals(Arrays.asList(42, 100), integers);
    }

    // ========== skip and take ==========
    @Test
    void testSkip() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5);

        assertEquals(Arrays.asList(3, 4, 5), numbers.skip(2));
        assertEquals(0, numbers.skip(10).size());
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), numbers.skip(0));
    }

    @Test
    void testTake() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5);

        assertEquals(Arrays.asList(1, 2, 3), numbers.take(3));
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), numbers.take(10));
        assertEquals(0, numbers.take(0).size());
    }

    @Test
    void testSkipAndTake() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        BetterList<Integer> result = numbers.skip(2).take(5);
        assertEquals(Arrays.asList(3, 4, 5, 6, 7), result);
    }

    // ========== reverse ==========
    @Test
    void testReverse() {
        assertEquals(Arrays.asList(5, 4, 3, 2, 1), createIterable(1, 2, 3, 4, 5).reverse());
        assertEquals(0, createIterable().reverse().size());
        assertEquals(Arrays.asList(42), createIterable(42).reverse());
    }

    // ========== unique / uniqueBy ==========
    @Test
    void testUnique() {
        BetterIterable<Integer> withDuplicates = createIterable(1, 2, 2, 3, 1, 4, 3, 5);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), withDuplicates.unique());
    }

    @Test
    void testUniqueBy() {
        BetterIterable<String> words = createStringIterable("cat", "car", "dog", "door", "cab");
        assertEquals(Arrays.asList("cat", "dog"), words.uniqueBy(s -> s.charAt(0)));
    }

    @Test
    void testUniqueByLength() {
        BetterIterable<String> words = createStringIterable("a", "bb", "ccc", "d", "ee", "fff");
        assertEquals(Arrays.asList("a", "bb", "ccc"), words.uniqueBy(String::length));
    }

    // ========== sort ==========
    @Test
    void testSort() {
        assertEquals(Arrays.asList(1, 2, 3, 5, 8, 9), createIterable(5, 2, 8, 1, 9, 3).sorted());
        assertEquals(Arrays.asList("apple", "banana", "cherry", "zebra"),
                createStringIterable("zebra", "apple", "banana", "cherry").sorted());
    }

    @Test
    void testSortWithComparator() {
        assertEquals(Arrays.asList(9, 8, 5, 3, 2, 1),
                createIterable(5, 2, 8, 1, 9, 3).sorted(Comparator.reverseOrder()));
    }

    @Test
    void testSortEmpty() {
        assertEquals(0, createIterable().sorted().size());
    }

    @Test
    void testSortBy() {
        BetterIterable<String> words = createStringIterable("zzz", "a", "bb", "cccc");
        assertEquals(Arrays.asList("a", "bb", "zzz", "cccc"), words.sortedBy(String::length));
    }

    @Test
    void testSortByWithComparator() {
        BetterIterable<String> words = createStringIterable("zzz", "a", "bb", "cccc");
        assertEquals(Arrays.asList("cccc", "zzz", "bb", "a"),
                words.sortedBy(String::length, Comparator.reverseOrder()));
    }

    // ========== toMap (one-per-key) ==========
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
        BetterMap<Character, String> map = createStringIterable().toMap(s -> s.charAt(0));
        assertEquals(0, map.size());
    }

    @Test
    void testToMapWithValueFn() {
        BetterIterable<String> words = createStringIterable("cat", "dog", "bird");
        BetterMap<Character, Integer> map = words.toMap(s -> s.charAt(0), String::length);
        assertEquals(3, map.get('c'));
        assertEquals(3, map.get('d'));
        assertEquals(4, map.get('b'));
    }

    // ========== asMap (elements-as-keys) ==========
    @Test
    void testAsMap() {
        BetterIterable<String> words = createStringIterable("cat", "dog", "bird");
        BetterMap<String, Integer> map = words.asMap(String::length);

        assertEquals(3, map.size());
        assertEquals(3, map.get("cat"));
        assertEquals(3, map.get("dog"));
        assertEquals(4, map.get("bird"));
    }

    @Test
    void testAsMapEmpty() {
        BetterMap<String, Integer> map = createStringIterable().asMap(String::length);
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
        BetterMap<Character, BetterList<String>> grouped = createStringIterable().groupBy(s -> s.charAt(0));
        assertEquals(0, grouped.size());
    }

    // ========== fold ==========
    @Test
    void testFold() {
        assertEquals(15, createIterable(1, 2, 3, 4, 5).fold(0, Integer::sum));
        assertEquals(24, createIterable(2, 3, 4).fold(1, (a, b) -> a * b));
        assertEquals("hello world", createStringIterable("hello", " ", "world").fold("", (acc, s) -> acc + s));
        assertEquals(100, createIterable().fold(100, Integer::sum));
    }

    // ========== count ==========
    @Test
    void testCount() {
        assertEquals(5, createIterable(1, 2, 3, 4, 5).count());
        assertEquals(0, createIterable().count());
        assertEquals(5, createIterable(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).filter(n -> n % 2 == 0).count());
    }

    // ========== first / last (null on empty) and OrThrow variants ==========
    @Test
    void testFirst() {
        assertEquals(1, createIterable(1, 2, 3, 4, 5).first());
        assertNull(createIterable().first());
    }

    @Test
    void testFirstOrThrow() {
        assertEquals(1, createIterable(1, 2, 3, 4, 5).firstOrThrow());
        assertThrows(NoSuchElementException.class, () -> createIterable().firstOrThrow());
    }

    @Test
    void testLast() {
        assertEquals(5, createIterable(1, 2, 3, 4, 5).last());
        assertNull(createIterable().last());
    }

    @Test
    void testLastOrThrow() {
        assertEquals(5, createIterable(1, 2, 3, 4, 5).lastOrThrow());
        assertThrows(NoSuchElementException.class, () -> createIterable().lastOrThrow());
    }

    // ========== all, hasMatch, hasNoMatch ==========
    @Test
    void testAll() {
        BetterIterable<Integer> evens = createIterable(2, 4, 6, 8, 10);
        assertTrue(evens.all(n -> n % 2 == 0));
        assertFalse(evens.all(n -> n > 5));
        assertTrue(createIterable().all(n -> false)); // vacuous truth
    }

    @Test
    void testHasMatch() {
        BetterIterable<Integer> numbers = createIterable(1, 2, 3, 4, 5);
        assertTrue(numbers.hasMatch(n -> n % 2 == 0));
        assertTrue(numbers.hasMatch(n -> n > 4));
        assertFalse(numbers.hasMatch(n -> n > 10));
        assertFalse(createIterable().hasMatch(n -> true));
    }

    @Test
    void testHasNoMatch() {
        BetterIterable<Integer> odds = createIterable(1, 3, 5, 7, 9);
        assertTrue(odds.hasNoMatch(n -> n % 2 == 0));
        assertFalse(odds.hasNoMatch(n -> n > 5));
        assertTrue(createIterable().hasNoMatch(n -> true)); // vacuous truth
    }

    // ========== indexOf ==========
    @Test
    void testIndexOfValue() {
        assertEquals(0, createIterable(1, 2, 3, 4, 5).indexOf(1));
        assertEquals(2, createIterable(1, 2, 3, 4, 5).indexOf(3));
        assertEquals(-1, createIterable(1, 2, 3, 4, 5).indexOf(99));
    }

    @Test
    void testIndexOfPredicate() {
        assertEquals(1, createIterable(1, 2, 3, 4, 5).indexOf((Integer n) -> n % 2 == 0));
        assertEquals(-1, createIterable(1, 3, 5).indexOf((Integer n) -> n % 2 == 0));
    }

    // ========== chained operations ==========
    @Test
    void testChainedOperations() {
        BetterList<Integer> result = createIterable(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .filter(n -> n % 2 == 0)
                .map(n -> n * 2)
                .take(3);

        assertEquals(Arrays.asList(4, 8, 12), result);
    }

    @Test
    void testComplexChainedOperations() {
        BetterIterable<String> words = createStringIterable("apple", "banana", "apricot", "cherry", "avocado", "blueberry");

        BetterList<String> result = words
                .filter(s -> s.startsWith("a") || s.startsWith("b"))
                .map(String::toUpperCase)
                .unique()
                .sorted();

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
        BetterList<Integer> list = createIterable(1, 2, 3, 4, 5).toList();
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
        assertEquals(0, createIterable().toList().size());
    }

    // ========== forEach ==========
    @Test
    void testForEach() {
        List<Integer> collected = new ArrayList<>();
        createIterable(1, 2, 3, 4, 5).forEach(collected::add);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), collected);
    }
}
