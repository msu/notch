package edu.montana.notch.util;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Predicate;

import static edu.montana.notch.AssertContains.assertContains;
import static org.junit.jupiter.api.Assertions.*;

class BetterMapTest {

    @Test
    void testConstructors() {
        BetterMap<String, Integer> emptyMap = new BetterMap<>();
        assertEquals(0, emptyMap.size());
        assertTrue(emptyMap.isEmpty());

        Map<String, Integer> sourceMap = new HashMap<>();
        sourceMap.put("a", 1);
        sourceMap.put("b", 2);
        sourceMap.put("c", 3);

        BetterMap<String, Integer> fromMap = new BetterMap<>(sourceMap);
        assertEquals(3, fromMap.size());
        assertEquals(Integer.valueOf(1), fromMap.get("a"));

        BetterMap<String, Integer> betterFactory = BetterMap.better(sourceMap);
        assertEquals(3, betterFactory.size());
    }

    @Test
    void testBasicMapOperations() {
        BetterMap<String, Integer> map = new BetterMap<>();

        assertNull(map.put("first", 1));
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(1), map.get("first"));

        assertEquals(Integer.valueOf(1), map.put("first", 10));
        assertEquals(1, map.size());
        assertEquals(Integer.valueOf(10), map.get("first"));

        map.put("second", 2);
        assertEquals(2, map.size());

        assertEquals(Integer.valueOf(10), map.remove("first"));
        assertEquals(1, map.size());
        assertFalse(map.containsKey("first"));

        assertNull(map.remove("nonexistent"));
        assertEquals(1, map.size());
    }

    @Test
    void testKeySetAndEntrySet() {
        BetterMap<String, Integer> map = new BetterMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        BetterSet<String> keySet = map.keySet();
        assertEquals(3, keySet.size());
        assertContains("a", keySet);
        assertContains("b", keySet);
        assertContains("c", keySet);

        BetterSet<Map.Entry<String, Integer>> entrySet = map.entrySet();
        assertEquals(3, entrySet.size());

        Map.Entry<String, Integer> foundEntry = entrySet.firstWhere(entry -> entry.getKey().equals("a"));
        assertNotNull(foundEntry);
        assertEquals(Integer.valueOf(1), foundEntry.getValue());
    }

    @Test
    void testFilterAsMap() {
        BetterMap<String, Integer> map = new BetterMap<>();
        map.put("apple", 5);
        map.put("banana", 6);
        map.put("cherry", 6);
        map.put("date", 4);

        Predicate<Map.Entry<String, Integer>> valueGreaterThanFour = entry -> entry.getValue() > 4;
        BetterMap<String, Integer> filtered = map.filterAsMap(valueGreaterThanFour);

        assertEquals(3, filtered.size());
        assertTrue(filtered.containsKey("apple"));
        assertTrue(filtered.containsKey("banana"));
        assertTrue(filtered.containsKey("cherry"));
        assertFalse(filtered.containsKey("date"));

        Predicate<Map.Entry<String, Integer>> keyStartsWithC = entry -> entry.getKey().startsWith("c");
        BetterMap<String, Integer> filteredByKey = map.filterAsMap(keyStartsWithC);

        assertEquals(1, filteredByKey.size());
        assertTrue(filteredByKey.containsKey("cherry"));
        assertEquals(Integer.valueOf(6), filteredByKey.get("cherry"));
    }

    @Test
    void testFilterByValues() {
        BetterMap<String, Integer> map = new BetterMap<>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        map.put("four", 4);
        map.put("five", 5);

        BetterMap<String, Integer> evenValues = map.filterByValues(v -> v % 2 == 0);
        assertEquals(2, evenValues.size());
        assertTrue(evenValues.containsKey("two"));
        assertTrue(evenValues.containsKey("four"));
        assertEquals(Integer.valueOf(2), evenValues.get("two"));
        assertEquals(Integer.valueOf(4), evenValues.get("four"));

        BetterMap<String, Integer> valuesGreaterThanThree = map.filterByValues(v -> v > 3);
        assertEquals(2, valuesGreaterThanThree.size());
        assertTrue(valuesGreaterThanThree.containsKey("four"));
        assertTrue(valuesGreaterThanThree.containsKey("five"));

        BetterMap<String, Integer> noMatches = map.filterByValues(v -> v > 10);
        assertEquals(0, noMatches.size());
        assertTrue(noMatches.isEmpty());
    }

    @Test
    void testFilterByKeys() {
        BetterMap<String, Integer> map = new BetterMap<>();
        map.put("apple", 1);
        map.put("banana", 2);
        map.put("apricot", 3);
        map.put("cherry", 4);

        BetterMap<String, Integer> startsWithA = map.filterByKeys(k -> k.startsWith("a"));
        assertEquals(2, startsWithA.size());
        assertTrue(startsWithA.containsKey("apple"));
        assertTrue(startsWithA.containsKey("apricot"));
        assertEquals(Integer.valueOf(1), startsWithA.get("apple"));
        assertEquals(Integer.valueOf(3), startsWithA.get("apricot"));

        BetterMap<String, Integer> longKeys = map.filterByKeys(k -> k.length() > 5);
        assertEquals(3, longKeys.size());
        assertTrue(longKeys.containsKey("banana"));
        assertTrue(longKeys.containsKey("apricot"));
        assertTrue(longKeys.containsKey("cherry"));

        BetterMap<String, Integer> noMatches = map.filterByKeys(k -> k.startsWith("z"));
        assertEquals(0, noMatches.size());
        assertTrue(noMatches.isEmpty());
    }

    @Test
    void testMapValues() {
        BetterMap<String, Integer> map = new BetterMap<>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);

        BetterMap<String, String> stringValues = map.mapValues(String::valueOf);
        assertEquals(3, stringValues.size());
        assertEquals("1", stringValues.get("one"));
        assertEquals("2", stringValues.get("two"));
        assertEquals("3", stringValues.get("three"));

        BetterMap<String, Integer> doubled = map.mapValues(v -> v * 2);
        assertEquals(3, doubled.size());
        assertEquals(Integer.valueOf(2), doubled.get("one"));
        assertEquals(Integer.valueOf(4), doubled.get("two"));
        assertEquals(Integer.valueOf(6), doubled.get("three"));

        BetterMap<String, Boolean> isEven = map.mapValues(v -> v % 2 == 0);
        assertEquals(3, isEven.size());
        assertEquals(Boolean.FALSE, isEven.get("one"));
        assertEquals(Boolean.TRUE, isEven.get("two"));
        assertEquals(Boolean.FALSE, isEven.get("three"));
    }

    @Test
    void testCopy() {
        BetterMap<String, Integer> map = new BetterMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        BetterMap<String, Integer> copy = map.copy();

        assertEquals(map.size(), copy.size());
        assertEquals(map, copy);
        assertNotSame(map, copy);

        copy.put("d", 4);
        assertEquals(3, map.size());
        assertEquals(4, copy.size());
        assertFalse(map.containsKey("d"));
        assertTrue(copy.containsKey("d"));
    }

    @Test
    void testConcat() {
        BetterMap<String, Integer> map1 = new BetterMap<>();
        map1.put("a", 1);
        map1.put("b", 2);

        BetterMap<String, Integer> map2 = new BetterMap<>();
        map2.put("c", 3);
        map2.put("d", 4);

        BetterMap<String, Integer> result = map1.concat(map2);
        assertEquals(4, result.size());
        assertEquals(Integer.valueOf(1), result.get("a"));
        assertEquals(Integer.valueOf(2), result.get("b"));
        assertEquals(Integer.valueOf(3), result.get("c"));
        assertEquals(Integer.valueOf(4), result.get("d"));

        assertEquals(2, map1.size());
        assertEquals(2, map2.size());
    }

    @Test
    void testConcatWithOverlappingKeys() {
        BetterMap<String, Integer> map1 = new BetterMap<>();
        map1.put("a", 1);
        map1.put("b", 2);

        BetterMap<String, Integer> map2 = new BetterMap<>();
        map2.put("b", 20);
        map2.put("c", 3);

        BetterMap<String, Integer> result = map1.concat(map2);
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(1), result.get("a"));
        assertEquals(Integer.valueOf(20), result.get("b"));
        assertEquals(Integer.valueOf(3), result.get("c"));
    }

    @Test
    void testBetterIterableMethods() {
        BetterMap<String, Integer> map = new BetterMap<>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        map.put("four", 4);

        BetterList<String> mappedKeys = map.map(entry -> entry.getKey().toUpperCase()).toList();
        assertEquals(4, mappedKeys.size());
        assertContains("ONE", mappedKeys);
        assertContains("TWO", mappedKeys);
        assertContains("THREE", mappedKeys);
        assertContains("FOUR", mappedKeys);

        BetterList<Map.Entry<String, Integer>> filteredEntries = map
                .filter(entry -> entry.getValue() % 2 == 0)
                .toList();
        assertEquals(2, filteredEntries.size());

        Set<Integer> evenValues = map.filter(entry -> entry.getValue() % 2 == 0)
                .map(Map.Entry::getValue)
                .toSet();
        assertEquals(Set.of(2, 4), evenValues);

        String joined = map.map(entry -> entry.getKey() + "=" + entry.getValue())
                          .toString(", ");
        assertTrue(joined.contains("one=1"));
        assertTrue(joined.contains("two=2"));
        assertTrue(joined.contains("three=3"));
        assertTrue(joined.contains("four=4"));
    }

    @Test
    void testMapInterfaceCompatibility() {
        BetterMap<String, Integer> betterMap = new BetterMap<>();
        Map<String, Integer> javaMap = betterMap;

        assertNull(javaMap.put("test1", 1));
        assertEquals(Integer.valueOf(1), javaMap.put("test1", 10));
        assertEquals(1, javaMap.size());

        javaMap.put("test2", 2);
        javaMap.put("test3", 3);
        assertEquals(3, javaMap.size());

        assertTrue(javaMap.containsKey("test1"));
        assertTrue(javaMap.containsValue(10));
        assertFalse(javaMap.isEmpty());

        Set<String> keySet = javaMap.keySet();
        assertEquals(3, keySet.size());
        assertContains("test1", keySet);

        Collection<Integer> values = javaMap.values();
        assertEquals(3, values.size());
        assertContains(10, values);

        Set<Map.Entry<String, Integer>> entrySet = javaMap.entrySet();
        assertEquals(3, entrySet.size());

        javaMap.clear();
        assertTrue(javaMap.isEmpty());
    }

    @Test
    void testIteratorAndForEach() {
        BetterMap<String, Integer> map = new BetterMap<>();
        map.put("x", 1);
        map.put("y", 2);
        map.put("z", 3);

        Set<String> collectedKeys = new HashSet<>();
        Set<Integer> collectedValues = new HashSet<>();

        for (Map.Entry<String, Integer> entry : map) {
            collectedKeys.add(entry.getKey());
            collectedValues.add(entry.getValue());
        }

        assertEquals(Set.of("x", "y", "z"), collectedKeys);
        assertEquals(Set.of(1, 2, 3), collectedValues);

        collectedKeys.clear();
        collectedValues.clear();

        map.forEach(entry -> {
            collectedKeys.add(entry.getKey());
            collectedValues.add(entry.getValue());
        });

        assertEquals(Set.of("x", "y", "z"), collectedKeys);
        assertEquals(Set.of(1, 2, 3), collectedValues);
    }

    @Test
    void testEqualsAndHashCode() {
        BetterMap<String, Integer> map1 = new BetterMap<>();
        map1.put("a", 1);
        map1.put("b", 2);
        map1.put("c", 3);

        BetterMap<String, Integer> map2 = new BetterMap<>();
        map2.put("c", 3);
        map2.put("b", 2);
        map2.put("a", 1);

        Map<String, Integer> regularMap = new HashMap<>();
        regularMap.put("a", 1);
        regularMap.put("b", 2);
        regularMap.put("c", 3);

        assertEquals(map1, map2);
        assertEquals(map1, regularMap);
        assertEquals(map1.hashCode(), map2.hashCode());
    }

    @Test
    void testEmptyMapBehavior() {
        BetterMap<String, Integer> emptyMap = new BetterMap<>();

        assertEquals(0, emptyMap.size());
        assertTrue(emptyMap.isEmpty());
        assertFalse(emptyMap.containsKey("anything"));
        assertFalse(emptyMap.containsValue(1));
        assertNull(emptyMap.get("anything"));
        assertNull(emptyMap.first());
        assertNull(emptyMap.firstWhere(entry -> true));
        assertFalse(emptyMap.hasMatch(entry -> true));
        assertTrue(emptyMap.hasNoMatch(entry -> true));
        assertEquals("", emptyMap.toString(","));
        
        BetterMap<String, Integer> filteredEmpty = emptyMap.filterByValues(v -> true);
        assertTrue(filteredEmpty.isEmpty());

        BetterMap<String, String> mappedEmpty = emptyMap.mapValues(String::valueOf);
        assertTrue(mappedEmpty.isEmpty());
    }

    @Test
    void testChainedOperations() {
        BetterMap<String, Integer> map = new BetterMap<>();
        map.put("apple", 5);
        map.put("banana", 6);
        map.put("cherry", 4);
        map.put("date", 4);
        map.put("elderberry", 10);

        BetterMap<String, String> result = map
                .filterByValues(v -> v > 4)
                .filterByKeys(k -> k.length() > 5)
                .mapValues(v -> "value_" + v);

        assertEquals(2, result.size());
        assertEquals("value_6", result.get("banana"));
        assertEquals("value_10", result.get("elderberry"));
        assertFalse(result.containsKey("apple"));
        assertFalse(result.containsKey("cherry"));
        assertFalse(result.containsKey("date"));
    }
}