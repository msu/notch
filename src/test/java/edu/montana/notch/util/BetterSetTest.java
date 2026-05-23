package edu.montana.notch.util;

import org.junit.jupiter.api.Test;

import java.util.*;

import static edu.montana.notch.AssertContains.assertContains;
import static org.junit.jupiter.api.Assertions.*;

class BetterSetTest {

    @Test
    void testConstructors() {
        BetterSet<String> emptySet = new BetterSet<>();
        assertEquals(0, emptySet.size());

        List<String> sourceList = Arrays.asList("a", "b", "c", "a");
        BetterSet<String> fromCollection = new BetterSet<>(sourceList);
        assertEquals(3, fromCollection.size());
        assertContains("a", fromCollection);

        String[] array = {"x", "y", "z", "x"};
        BetterSet<String> fromArray = new BetterSet<>(array);
        assertEquals(3, fromArray.size());
        assertContains("x", fromArray);

        BetterSet<String> betterFactory = BetterSet.better(sourceList);
        assertEquals(3, betterFactory.size());
    }

    @Test
    void testBasicSetOperations() {
        BetterSet<String> set = new BetterSet<>();

        assertTrue(set.add("first"));
        assertEquals(1, set.size());
        assertContains("first", set);

        assertFalse(set.add("first"));
        assertEquals(1, set.size());

        assertTrue(set.add("second"));
        assertEquals(2, set.size());

        assertTrue(set.remove("first"));
        assertEquals(1, set.size());
        assertFalse(set.contains("first"));

        assertFalse(set.remove("nonexistent"));
        assertEquals(1, set.size());
    }

    @Test
    void testRemoveAll() {
        BetterSet<String> set = new BetterSet<>();
        set.addAll(Arrays.asList("a", "b", "c", "d"));
        Collection<String> toRemove = Arrays.asList("b", "d", "e");

        assertTrue(set.removeAll(toRemove));
        assertEquals(2, set.size());
        assertFalse(set.contains("b"));
        assertFalse(set.contains("d"));
        assertContains("a", set);
        assertContains("c", set);
    }

    @Test
    void testCopy() {
        BetterSet<String> set = new BetterSet<>();
        set.addAll(Arrays.asList("a", "b", "c"));
        BetterSet<String> copy = set.copy();

        assertEquals(set.size(), copy.size());
        assertEquals(set, copy);
        assertNotSame(set, copy);

        copy.add("d");
        assertEquals(3, set.size());
        assertEquals(4, copy.size());
    }

    @Test
    void testUnion() {
        BetterSet<String> set1 = new BetterSet<>(Arrays.asList("a", "b", "c"));
        BetterSet<String> set2 = new BetterSet<>(Arrays.asList("c", "d", "e"));

        BetterSet<String> union = set1.union(set2);
        assertEquals(5, union.size());
        assertContains("a", union);
        assertContains("b", union);
        assertContains("c", union);
        assertContains("d", union);
        assertContains("e", union);

        assertEquals(3, set1.size());
        assertEquals(3, set2.size());
    }

    @Test
    void testIntersect() {
        BetterSet<String> set1 = new BetterSet<>(Arrays.asList("a", "b", "c", "d"));
        BetterSet<String> set2 = new BetterSet<>(Arrays.asList("c", "d", "e", "f"));

        BetterSet<String> intersection = set1.intersect(set2);
        assertEquals(2, intersection.size());
        assertContains("c", intersection);
        assertContains("d", intersection);
        assertFalse(intersection.contains("a"));
        assertFalse(intersection.contains("e"));

        assertEquals(4, set1.size());
        assertEquals(4, set2.size());
    }

    @Test
    void testIntersectWithEmptySet() {
        BetterSet<String> set1 = new BetterSet<>(Arrays.asList("a", "b", "c"));
        BetterSet<String> emptySet = new BetterSet<>();

        BetterSet<String> intersection = set1.intersect(emptySet);
        assertEquals(0, intersection.size());
        assertTrue(intersection.isEmpty());
    }

    @Test
    void testIntersectWithNoOverlap() {
        BetterSet<String> set1 = new BetterSet<>(Arrays.asList("a", "b", "c"));
        BetterSet<String> set2 = new BetterSet<>(Arrays.asList("x", "y", "z"));

        BetterSet<String> intersection = set1.intersect(set2);
        assertEquals(0, intersection.size());
        assertTrue(intersection.isEmpty());
    }

    @Test
    void testFilterAsSet() {
        BetterSet<Integer> intSet = new BetterSet<>(Arrays.asList(1, 2, 3, 4, 5, 6));

        BetterSet<Integer> evenNumbers = intSet.filterAsSet(n -> n % 2 == 0);
        assertEquals(3, evenNumbers.size());
        assertContains(2, evenNumbers);
        assertContains(4, evenNumbers);
        assertContains(6, evenNumbers);
        assertFalse(evenNumbers.contains(1));
        assertFalse(evenNumbers.contains(3));
        assertFalse(evenNumbers.contains(5));

        BetterSet<Integer> greaterThanThree = intSet.filterAsSet(n -> n > 3);
        assertEquals(3, greaterThanThree.size());
        assertContains(4, greaterThanThree);
        assertContains(5, greaterThanThree);
        assertContains(6, greaterThanThree);

        BetterSet<Integer> noMatches = intSet.filterAsSet(n -> n > 10);
        assertEquals(0, noMatches.size());
        assertTrue(noMatches.isEmpty());
    }

    @Test
    void testBetterIterableMethods() {
        BetterSet<Integer> intSet = new BetterSet<>(Arrays.asList(1, 2, 3, 4, 5));

        BetterList<String> mapped = intSet.map(String::valueOf).toList();
        assertEquals(5, mapped.size());
        assertContains("3", mapped);

        Set<Integer> toSet = intSet.toSet();
        assertEquals(5, toSet.size());
        assertContains(3, toSet);

        BetterList<Integer> filtered = intSet.filter(n -> n % 2 == 0).toList();
        assertEquals(2, filtered.size());
        assertTrue(filtered.contains(2));
        assertTrue(filtered.contains(4));
        
        String joined = intSet.toString(", ");
        assertTrue(joined.contains("1"));
        assertTrue(joined.contains("2"));
        assertTrue(joined.contains("3"));
        assertTrue(joined.contains("4"));
        assertTrue(joined.contains("5"));
        
        Integer first = intSet.first();
        assertNotNull(first);
        assertContains(first, intSet);

        Integer firstEven = intSet.firstWhere(n -> n % 2 == 0);
        assertNotNull(firstEven);
        assertTrue(firstEven % 2 == 0);

        assertTrue(intSet.hasAny(n -> n > 3));
        assertFalse(intSet.hasAny(n -> n > 10));
        assertTrue(intSet.hasNone(n -> n > 10));
        assertFalse(intSet.hasNone(n -> n > 3));
    }

    @Test
    void testToStringWithSeparator() {
        BetterSet<String> set = new BetterSet<>(Arrays.asList("a", "b", "c"));
        String result = set.toString("|");
        assertEquals(5, result.length());
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
        assertTrue(result.contains("c"));
        assertTrue(result.contains("|"));
    }

    @Test
    void testToMaps() {
        BetterSet<String> words = new BetterSet<>(Arrays.asList("cat", "dog", "car", "door", "cab"));

        BetterMap<Integer, BetterList<String>> byLength = words.groupBy(String::length);
        assertEquals(2, byLength.size());
        assertEquals(4, byLength.get(3).size());
        assertEquals(1, byLength.get(4).size());

        BetterMap<Integer, BetterList<String>> orderedByLength = words.groupBy(String::length, Comparator.naturalOrder());
        assertEquals(Arrays.asList(3, 4), new ArrayList<>(orderedByLength.keySet()));

        BetterMap<Character, String> distinctByFirstChar = words.toMap(s -> s.charAt(0));
        assertTrue(distinctByFirstChar.containsKey('c'));
        assertTrue(distinctByFirstChar.containsKey('d'));

        BetterMap<Character, String> orderedDistinctByFirstChar = words.toMap(s -> s.charAt(0), Comparator.naturalOrder());
        assertEquals(Arrays.asList('c', 'd'), new ArrayList<>(orderedDistinctByFirstChar.keySet()));
    }

    @Test
    void testSetInterfaceCompatibility() {
        BetterSet<String> betterSet = new BetterSet<>();
        Set<String> javaSet = betterSet;

        assertTrue(javaSet.add("test1"));
        assertFalse(javaSet.add("test1"));
        assertEquals(1, javaSet.size());

        javaSet.addAll(Arrays.asList("test2", "test3", "test1"));
        assertEquals(3, javaSet.size());

        assertContains("test1", javaSet);
        assertFalse(javaSet.isEmpty());

        Iterator<String> iterator = javaSet.iterator();
        assertTrue(iterator.hasNext());
        String first = iterator.next();
        assertNotNull(first);

        String[] array = javaSet.toArray(new String[0]);
        assertEquals(3, array.length);

        javaSet.clear();
        assertTrue(javaSet.isEmpty());
    }

    @Test
    void testCollectionInterfaceCompatibility() {
        BetterSet<String> betterSet = new BetterSet<>();
        Collection<String> collection = betterSet;

        assertTrue(collection.add("item"));
        assertFalse(collection.add("item"));
        assertTrue(collection.addAll(Arrays.asList("a", "b", "c", "a")));
        assertEquals(4, collection.size());

        assertContains("item", collection);
        assertTrue(collection.containsAll(Arrays.asList("a", "b")));

        assertFalse(collection.remove("nonexistent"));
        assertTrue(collection.remove("item"));
        assertEquals(3, collection.size());

        assertTrue(collection.retainAll(Arrays.asList("a", "c", "x")));
        assertEquals(2, collection.size());
        assertFalse(collection.contains("b"));
    }

    @Test
    void testIteratorAndForEach() {
        BetterSet<String> set = new BetterSet<>(Arrays.asList("x", "y", "z"));

        Set<String> collected = new HashSet<>();
        for (String item : set) {
            collected.add(item);
        }
        assertEquals(3, collected.size());
        assertContains("x", collected);
        assertContains("y", collected);
        assertContains("z", collected);

        collected.clear();
        set.forEach(collected::add);
        assertEquals(3, collected.size());
    }

    @Test
    void testEqualsAndHashCode() {
        BetterSet<String> set1 = new BetterSet<>(Arrays.asList("a", "b", "c"));
        BetterSet<String> set2 = new BetterSet<>(Arrays.asList("c", "b", "a"));
        Set<String> regularSet = new HashSet<>(Arrays.asList("a", "b", "c"));

        assertEquals(set1, set2);
        assertEquals(set1, regularSet);
        assertEquals(set1.hashCode(), set2.hashCode());
    }

    @Test
    void testSetUniquenessBehavior() {
        BetterSet<String> set = new BetterSet<>();

        assertTrue(set.add("duplicate"));
        assertFalse(set.add("duplicate"));
        assertEquals(1, set.size());

        set.addAll(Arrays.asList("duplicate", "unique", "duplicate"));
        assertEquals(2, set.size());
        assertContains("duplicate", set);
        assertContains("unique", set);
    }

    @Test
    void testEmptySetBehavior() {
        BetterSet<String> emptySet = new BetterSet<>();

        assertEquals(0, emptySet.size());
        assertTrue(emptySet.isEmpty());
        assertFalse(emptySet.contains("anything"));
        assertNull(emptySet.first());
        assertNull(emptySet.firstWhere(s -> true));
        assertFalse(emptySet.hasMatch(s -> true));
        assertTrue(emptySet.hasNoMatch(s -> true));
        assertEquals("", emptySet.toString(","));
    }

    @Test
    void testSetOperationsWithSelf() {
        BetterSet<String> set = new BetterSet<>(Arrays.asList("a", "b", "c"));

        BetterSet<String> unionWithSelf = set.union(set);
        assertEquals(3, unionWithSelf.size());
        assertEquals(set, unionWithSelf);

        BetterSet<String> intersectWithSelf = set.intersect(set);
        assertEquals(3, intersectWithSelf.size());
        assertEquals(set, intersectWithSelf);
    }
}