package edu.montana.notch.util;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class BetterListTest {

    @Test
    void testConstructors() {
        BetterList<String> emptyList = new BetterList<>();
        assertEquals(0, emptyList.size());

        List<String> sourceList = Arrays.asList("a", "b", "c");
        BetterList<String> fromCollection = new BetterList<>(sourceList);
        assertEquals(3, fromCollection.size());
        assertEquals("a", fromCollection.get(0));

        String[] array = {"x", "y", "z"};
        BetterList<String> fromArray = new BetterList<>(array);
        assertEquals(3, fromArray.size());
        assertEquals("x", fromArray.get(0));

        BetterList<String> betterFactory = BetterList.better(sourceList);
        assertEquals(3, betterFactory.size());
    }

    @Test
    void testBasicListOperations() {
        BetterList<String> list = new BetterList<>();
        
        assertTrue(list.add("first"));
        assertEquals(1, list.size());
        assertEquals("first", list.get(0));

        list.add("second");
        list.add(1, "middle");
        assertEquals(3, list.size());
        assertEquals("middle", list.get(1));

        String removed = list.remove(1);
        assertEquals("middle", removed);
        assertEquals(2, list.size());

        String replaced = list.set(0, "new_first");
        assertEquals("first", replaced);
        assertEquals("new_first", list.get(0));
    }

    @Test
    void testRemoveAll() {
        BetterList<String> list = new BetterList<>();
        list.addAll(Arrays.asList("a", "b", "c", "d"));
        Collection<String> toRemove = Arrays.asList("b", "d");
        
        assertTrue(list.removeAll(toRemove));
        assertEquals(2, list.size());
        assertFalse(list.contains("b"));
        assertFalse(list.contains("d"));
        assertTrue(list.contains("a"));
        assertTrue(list.contains("c"));
    }

    @Test
    void testLast() {
        BetterList<String> list = new BetterList<>();
        assertNull(list.last());
        
        list.add("first");
        assertEquals("first", list.last());
        
        list.add("second");
        assertEquals("second", list.last());
    }

    @Test
    void testLastWhere() {
        BetterList<String> list = new BetterList<>();
        list.addAll(Arrays.asList("apple", "banana", "apricot", "cherry"));
        
        Predicate<String> startsWithA = s -> s.startsWith("a");
        assertEquals("apricot", list.lastWhere(startsWithA));
        
        Predicate<String> startsWithZ = s -> s.startsWith("z");
        assertNull(list.lastWhere(startsWithZ));
    }

    @Test
    void testDistinct() {
        BetterList<String> list = new BetterList<>();
        list.addAll(Arrays.asList("a", "b", "a", "c", "b", "d"));
        BetterList<String> distinct = list.distinct();
        
        assertEquals(4, distinct.size());
        assertEquals(Arrays.asList("a", "b", "c", "d"), distinct);
    }

    @Test
    void testDistinctWithFunction() {
        BetterList<String> words = new BetterList<>(Arrays.asList("cat", "dog", "car", "door"));
        BetterList<String> distinctByLength = words.distinct(String::length);
        
        assertEquals(2, distinctByLength.size());
        assertEquals("cat", distinctByLength.get(0));
        assertEquals("door", distinctByLength.get(1));
    }

    @Test
    void testCopy() {
        BetterList<String> list = new BetterList<>();
        list.addAll(Arrays.asList("a", "b", "c"));
        BetterList<String> copy = list.copy();
        
        assertEquals(list.size(), copy.size());
        assertEquals(list, copy);
        assertNotSame(list, copy);
        
        copy.add("d");
        assertEquals(3, list.size());
        assertEquals(4, copy.size());
    }

    @Test
    void testConcat() {
        BetterList<String> list = new BetterList<>();
        list.addAll(Arrays.asList("a", "b"));
        BetterList<String> other = new BetterList<>(Arrays.asList("c", "d"));
        
        BetterList<String> result = list.concat(other);
        assertEquals(4, result.size());
        assertEquals(Arrays.asList("a", "b", "c", "d"), result);
        
        assertEquals(2, list.size());
        assertEquals(2, other.size());
    }

    @Test
    void testBetterIterableMethods() {
        BetterList<Integer> intList = new BetterList<>();
        intList.addAll(Arrays.asList(1, 2, 3, 4, 5));
        
        BetterList<String> mapped = intList.map(String::valueOf);
        assertEquals(Arrays.asList("1", "2", "3", "4", "5"), mapped);
        
        Set<Integer> set = intList.toSet();
        assertEquals(5, set.size());
        assertTrue(set.contains(3));
        
        BetterList<Integer> filtered = intList.filter(n -> n % 2 == 0);
        assertEquals(Arrays.asList(2, 4), filtered);
        
        String joined = intList.join(", ");
        assertEquals("1, 2, 3, 4, 5", joined);
        
        assertEquals(Integer.valueOf(1), intList.first());
        assertEquals(Integer.valueOf(2), intList.firstWhere(n -> n % 2 == 0));
        
        assertTrue(intList.hasMatch(n -> n > 3));
        assertFalse(intList.hasMatch(n -> n > 10));
        assertTrue(intList.hasNoMatch(n -> n > 10));
        assertFalse(intList.hasNoMatch(n -> n > 3));
    }

    @Test
    void testToStringWithSeparator() {
        BetterList<String> list = new BetterList<>();
        list.addAll(Arrays.asList("a", "b", "c"));
        assertEquals("a|b|c", list.toString("|"));
        assertEquals("abc", list.toString(""));
    }

    @Test
    void testToMaps() {
        BetterList<String> words = new BetterList<>(Arrays.asList("cat", "dog", "car", "door", "cab"));
        
        Map<Integer, List<String>> byLength = words.toMap(String::length);
        assertEquals(2, byLength.size());
        assertEquals(Arrays.asList("cat", "dog", "car", "cab"), byLength.get(3));
        assertEquals(Arrays.asList("door"), byLength.get(4));
        
        TreeMap<Integer, List<String>> orderedByLength = words.toOrderedMap(String::length);
        assertTrue(orderedByLength instanceof TreeMap);
        
        Map<Character, String> distinctByFirstChar = words.toDistinctMap(s -> s.charAt(0));
        assertEquals("cab", distinctByFirstChar.get('c'));
        assertEquals("door", distinctByFirstChar.get('d'));
        
        TreeMap<Character, String> orderedDistinctByFirstChar = words.toOrderedDistinctMap(s -> s.charAt(0));
        assertTrue(orderedDistinctByFirstChar instanceof TreeMap);
    }

    @Test
    void testListInterfaceCompatibility() {
        BetterList<String> betterList = new BetterList<>();
        List<String> javaList = betterList;
        
        javaList.add("test1");
        javaList.add("test2");
        assertEquals(2, javaList.size());
        
        javaList.addAll(Arrays.asList("test3", "test4"));
        assertEquals(4, javaList.size());
        
        assertTrue(javaList.contains("test1"));
        assertFalse(javaList.isEmpty());
        
        Iterator<String> iterator = javaList.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("test1", iterator.next());
        
        String[] array = javaList.toArray(new String[0]);
        assertEquals(4, array.length);
        assertEquals("test1", array[0]);
        
        javaList.clear();
        assertTrue(javaList.isEmpty());
    }

    @Test
    void testCollectionInterfaceCompatibility() {
        BetterList<String> betterList = new BetterList<>();
        Collection<String> collection = betterList;
        
        assertTrue(collection.add("item"));
        assertTrue(collection.addAll(Arrays.asList("a", "b", "c")));
        assertEquals(4, collection.size());
        
        assertTrue(collection.contains("item"));
        assertTrue(collection.containsAll(Arrays.asList("a", "b")));
        
        assertFalse(collection.remove("nonexistent"));
        assertTrue(collection.remove("item"));
        assertEquals(3, collection.size());
        
       assertTrue(collection.retainAll(Arrays.asList("a", "c")));
        assertEquals(2, collection.size());
        assertFalse(collection.contains("b"));
    }

    @Test
    void testIteratorAndForEach() {
        BetterList<String> list = new BetterList<>();
        list.addAll(Arrays.asList("x", "y", "z"));
        
        List<String> collected = new ArrayList<>();
        for (String item : list) {
            collected.add(item);
        }
        assertEquals(Arrays.asList("x", "y", "z"), collected);
        
        collected.clear();
        list.forEach(collected::add);
        assertEquals(Arrays.asList("x", "y", "z"), collected);
    }

    @Test
    void testEqualsAndHashCode() {
        BetterList<String> list1 = new BetterList<>(Arrays.asList("a", "b", "c"));
        BetterList<String> list2 = new BetterList<>(Arrays.asList("a", "b", "c"));
        List<String> regularList = Arrays.asList("a", "b", "c");
        
        assertEquals(list1, list2);
        assertEquals(list1, regularList);
        assertEquals(list1.hashCode(), list2.hashCode());
    }

    @Test
    void testIndexOperations() {
        BetterList<String> list = new BetterList<>();
        list.addAll(Arrays.asList("a", "b", "c", "b", "d"));
        
        assertEquals(1, list.indexOf("b"));
        assertEquals(3, list.lastIndexOf("b"));
        assertEquals(-1, list.indexOf("nonexistent"));
        
        List<String> sublist = list.subList(1, 4);
        assertEquals(Arrays.asList("b", "c", "b"), sublist);
    }

    @Test
    void testListIterator() {
        BetterList<String> list = new BetterList<>();
        list.addAll(Arrays.asList("a", "b", "c"));
        
        ListIterator<String> listIterator = list.listIterator();
        assertTrue(listIterator.hasNext());
        assertEquals("a", listIterator.next());
        
        ListIterator<String> listIteratorFromIndex = list.listIterator(1);
        assertEquals("b", listIteratorFromIndex.next());
    }
}