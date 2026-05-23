package edu.montana.notch.util;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static edu.montana.notch.AssertContains.assertContains;
import static org.junit.jupiter.api.Assertions.*;

class BetterIterableTest {

    @Test
    void testBetterFactoryMethod() {
        List<String> sourceList = Arrays.asList("a", "b", "c");
        BetterIterable<String> betterIterable = BetterIterable.better(sourceList);

        assertNotNull(betterIterable);

        List<String> collected = new ArrayList<>();
        betterIterable.forEach(collected::add);
        assertEquals(Arrays.asList("a", "b", "c"), collected);
    }

    @Test
    void testMap() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        BetterIterable<Integer> betterNumbers = BetterIterable.better(numbers);

        BetterList<String> mapped = betterNumbers.map(String::valueOf).toList();
        assertEquals(5, mapped.size());
        assertEquals(Arrays.asList("1", "2", "3", "4", "5"), mapped);

        BetterList<Integer> doubled = betterNumbers.map(n -> n * 2).toList();
        assertEquals(Arrays.asList(2, 4, 6, 8, 10), doubled);
    }

    @Test
    void testToSet() {
        List<String> withDuplicates = Arrays.asList("a", "b", "a", "c", "b");
        BetterIterable<String> betterIterable = BetterIterable.better(withDuplicates);
        
        BetterSet<String> result = betterIterable.toSet();
        assertEquals(3, result.size());
        assertContains("a", result);
        assertContains("b", result);
        assertContains("c", result);
    }

    @Test
    void testToList() {
        Set<String> sourceSet = new HashSet<>(Arrays.asList("x", "y", "z"));
        BetterIterable<String> betterIterable = BetterIterable.better(sourceSet);

        BetterList<String> result = betterIterable.toList();
        assertEquals(3, result.size());
        assertContains("x", result);
        assertContains("y", result);
        assertContains("z", result);
    }

    @Test
    void testGroupBy() {
        List<String> words = Arrays.asList("cat", "dog", "car", "door", "cab");
        BetterIterable<String> betterWords = BetterIterable.better(words);

        BetterMap<Integer, BetterList<String>> byLength = betterWords.groupBy(String::length);
        assertEquals(2, byLength.size());
        assertEquals(4, byLength.get(3).size());
        assertEquals(Arrays.asList("cat", "dog", "car", "cab"), byLength.get(3));
        assertEquals(Arrays.asList("door"), byLength.get(4));
    }

    @Test
    void testGroupByOrdered() {
        List<String> words = Arrays.asList("zebra", "apple", "banana");
        BetterIterable<String> betterWords = BetterIterable.better(words);

        BetterMap<Character, BetterList<String>> orderedByFirstChar = betterWords.groupBy(s -> s.charAt(0), Comparator.<Character>naturalOrder());
        assertEquals(3, orderedByFirstChar.size());

        List<Character> keys = new ArrayList<>(orderedByFirstChar.keySet());
        assertEquals(Arrays.asList('a', 'b', 'z'), keys);
    }

    @Test
    void testGroupByOrderedWithComparator() {
        List<String> words = Arrays.asList("zebra", "apple", "banana");
        BetterIterable<String> betterWords = BetterIterable.better(words);

        Comparator<Character> reverseOrder = Comparator.<Character>naturalOrder().reversed();
        BetterMap<Character, BetterList<String>> orderedByFirstChar = betterWords.groupBy(s -> s.charAt(0), reverseOrder);

        List<Character> keys = new ArrayList<>(orderedByFirstChar.keySet());
        assertEquals(Arrays.asList('z', 'b', 'a'), keys);
    }

    @Test
    void testToMap() {
        List<String> words = Arrays.asList("cat", "car", "dog", "door");
        BetterIterable<String> betterWords = BetterIterable.better(words);

        BetterMap<Character, String> distinctByFirstChar = betterWords.toMap(s -> s.charAt(0));
        assertEquals(2, distinctByFirstChar.size());
        assertEquals("car", distinctByFirstChar.get('c'));
        assertEquals("door", distinctByFirstChar.get('d'));
    }

    @Test
    void testToMapOrdered() {
        List<String> words = Arrays.asList("zebra", "apple", "banana");
        BetterIterable<String> betterWords = BetterIterable.better(words);

        BetterMap<Character, String> orderedDistinct = betterWords.toMap(s -> s.charAt(0), Comparator.<Character>naturalOrder());
        assertEquals(3, orderedDistinct.size());

        List<Character> keys = new ArrayList<>(orderedDistinct.keySet());
        assertEquals(Arrays.asList('a', 'b', 'z'), keys);
    }

    @Test
    void testToMapOrderedWithComparator() {
        List<String> words = Arrays.asList("zebra", "apple", "banana");
        BetterIterable<String> betterWords = BetterIterable.better(words);

        Comparator<Character> reverseOrder = Comparator.<Character>naturalOrder().reversed();
        BetterMap<Character, String> orderedDistinct = betterWords.toMap(s -> s.charAt(0), reverseOrder);

        List<Character> keys = new ArrayList<>(orderedDistinct.keySet());
        assertEquals(Arrays.asList('z', 'b', 'a'), keys);
    }

    @Test
    void testFilter() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        BetterIterable<Integer> betterNumbers = BetterIterable.better(numbers);

        BetterList<Integer> evenNumbers = betterNumbers.filter(n -> n % 2 == 0).toList();
        assertEquals(Arrays.asList(2, 4, 6, 8, 10), evenNumbers);

        BetterList<Integer> greaterThanFive = betterNumbers.filter(n -> n > 5).toList();
        assertEquals(Arrays.asList(6, 7, 8, 9, 10), greaterThanFive);

        BetterList<Integer> noMatches = betterNumbers.filter(n -> n > 20).toList();
        assertEquals(0, noMatches.size());
    }

    @Test
    void testToStringWithSeparator() {
        List<String> items = Arrays.asList("apple", "banana", "cherry");
        BetterIterable<String> betterItems = BetterIterable.better(items);

        assertEquals("apple|banana|cherry", betterItems.toString("|"));
        assertEquals("apple, banana, cherry", betterItems.toString(", "));
        assertEquals("applebananacherry", betterItems.toString(""));
    }

    @Test
    void testToStringWithEmptyIterable() {
        List<String> emptyList = Arrays.asList();
        BetterIterable<String> betterEmpty = BetterIterable.better(emptyList);

        assertEquals("", betterEmpty.toString("|"));
        assertEquals("", betterEmpty.toString(", "));
    }

    @Test
    void testFirst() {
        List<String> items = Arrays.asList("first", "second", "third");
        BetterIterable<String> betterItems = BetterIterable.better(items);

        assertEquals("first", betterItems.first());

        List<String> emptyList = Arrays.asList();
        BetterIterable<String> betterEmpty = BetterIterable.better(emptyList);
        assertNull(betterEmpty.first());
    }

    @Test
    void testFirstWhere() {
        List<String> words = Arrays.asList("apple", "banana", "apricot", "cherry");
        BetterIterable<String> betterWords = BetterIterable.better(words);

        Predicate<String> startsWithA = s -> s.startsWith("a");
        assertEquals("apple", betterWords.firstWhere(startsWithA));

        Predicate<String> startsWithZ = s -> s.startsWith("z");
        assertNull(betterWords.firstWhere(startsWithZ));

        List<String> emptyList = Arrays.asList();
        BetterIterable<String> betterEmpty = BetterIterable.better(emptyList);
        assertNull(betterEmpty.firstWhere(startsWithA));
    }

    @Test
    void testHasMatch() {
        List<Integer> numbers = Arrays.asList(1, 3, 5, 7, 9);
        BetterIterable<Integer> betterNumbers = BetterIterable.better(numbers);

        assertTrue(betterNumbers.hasMatch(n -> n > 5));
        assertTrue(betterNumbers.hasMatch(n -> n == 1));
        assertFalse(betterNumbers.hasMatch(n -> n % 2 == 0));
        assertFalse(betterNumbers.hasMatch(n -> n > 20));

        List<Integer> emptyList = Arrays.asList();
        BetterIterable<Integer> betterEmpty = BetterIterable.better(emptyList);
        assertFalse(betterEmpty.hasMatch(n -> true));
    }

    @Test
    void testHasNoMatch() {
        List<Integer> numbers = Arrays.asList(1, 3, 5, 7, 9);
        BetterIterable<Integer> betterNumbers = BetterIterable.better(numbers);

        assertTrue(betterNumbers.hasNoMatch(n -> n % 2 == 0));
        assertTrue(betterNumbers.hasNoMatch(n -> n > 20));
        assertFalse(betterNumbers.hasNoMatch(n -> n > 5));
        assertFalse(betterNumbers.hasNoMatch(n -> n == 1));

        List<Integer> emptyList = Arrays.asList();
        BetterIterable<Integer> betterEmpty = BetterIterable.better(emptyList);
        assertTrue(betterEmpty.hasNoMatch(n -> true));
    }

    @Test
    void testJoin() {
        List<String> items = Arrays.asList("apple", "banana", "cherry");
        BetterIterable<String> betterItems = BetterIterable.better(items);
        
        assertEquals("apple, banana, cherry", betterItems.toString(", "));
        assertEquals("apple|banana|cherry", betterItems.toString("|"));
        assertEquals("applebananacherry", betterItems.toString(""));
        
        List<Integer> numbers = Arrays.asList(1, 2, 3);
        BetterIterable<Integer> betterNumbers = BetterIterable.better(numbers);
        assertEquals("1-2-3", betterNumbers.toString("-"));
    }

    @Test
    void testJoinWithEmptyIterable() {
        List<String> emptyList = Arrays.asList();
        BetterIterable<String> betterEmpty = BetterIterable.better(emptyList);
        
        assertEquals("", betterEmpty.toString(", "));
        assertEquals("", betterEmpty.toString("|"));
    }

    @Test
    void testJoinWithSingleElement() {
        List<String> singleItem = Arrays.asList("alone");
        BetterIterable<String> betterSingle = BetterIterable.better(singleItem);
        
        assertEquals("alone", betterSingle.toString(", "));
        assertEquals("alone", betterSingle.toString("|"));
    }

    @Test
    void testChainedOperations() {
        List<String> words = Arrays.asList("apple", "banana", "apricot", "cherry", "avocado");
        BetterIterable<String> betterWords = BetterIterable.better(words);

        BetterList<String> result = betterWords
                .filter(s -> s.startsWith("a"))
                .map(String::toUpperCase)
                .toList();

        assertEquals(Arrays.asList("APPLE", "APRICOT", "AVOCADO"), result);

        Set<Integer> lengths = betterWords
                .filter(s -> s.length() > 5)
                .map(String::length)
                .toSet();

        assertEquals(Set.of(6, 7), lengths);
    }

    @Test
    void testWithDifferentIterableTypes() {
        Set<String> sourceSet = new HashSet<>(Arrays.asList("x", "y", "z"));
        BetterIterable<String> fromSet = BetterIterable.better(sourceSet);

        BetterList<String> mapped = fromSet.map(String::toUpperCase).toList();
        assertEquals(3, mapped.size());
        assertContains("X", mapped);
        assertContains("Y", mapped);
        assertContains("Z", mapped);

        Queue<Integer> sourceQueue = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        BetterIterable<Integer> fromQueue = BetterIterable.better(sourceQueue);
        
        String joined = fromQueue.toString("-");
        assertEquals("1-2-3", joined);
    }

    @Test
    void testIteratorBehavior() {
        List<String> items = Arrays.asList("a", "b", "c");
        BetterIterable<String> betterItems = BetterIterable.better(items);

        Iterator<String> iterator = betterItems.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("b", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("c", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void testForEachLoop() {
        List<String> items = Arrays.asList("x", "y", "z");
        BetterIterable<String> betterItems = BetterIterable.better(items);

        List<String> collected = new ArrayList<>();
        for (String item : betterItems) {
            collected.add(item);
        }

        assertEquals(Arrays.asList("x", "y", "z"), collected);
    }

    @Test
    void testFilterByType() {
        List<Object> mixedObjects = Arrays.asList("hello", 42, "world", 3.14, 100, "test");
        BetterIterable<Object> betterMixed = BetterIterable.better(mixedObjects);
        
        BetterList<String> strings = betterMixed.filter(String.class);
        assertEquals(Arrays.asList("hello", "world", "test"), strings);
        
        BetterList<Integer> integers = betterMixed.filter(Integer.class);
        assertEquals(Arrays.asList(42, 100), integers);
        
        BetterList<Double> doubles = betterMixed.filter(Double.class);
        assertEquals(Arrays.asList(3.14), doubles);
        
        BetterList<Boolean> booleans = betterMixed.filter(Boolean.class);
        assertEquals(0, booleans.size());
    }

    @Test
    void testFilterByTypeWithInheritance() {
        List<Object> objects = Arrays.asList("string", new ArrayList<>(), new LinkedList<>(), 42);
        BetterIterable<Object> betterObjects = BetterIterable.better(objects);
        
        BetterList<List> lists = betterObjects.filter(List.class);
        assertEquals(2, lists.size());
        assertTrue(lists.get(0) instanceof ArrayList);
        assertTrue(lists.get(1) instanceof LinkedList);
        
        BetterList<ArrayList> arrayLists = betterObjects.filter(ArrayList.class);
        assertEquals(1, arrayLists.size());
        assertInstanceOf(ArrayList.class, arrayLists.get(0));
    }

    @Test
    void testFilterByTypeWithEmptyIterable() {
        List<Object> emptyList = Arrays.asList();
        BetterIterable<Object> betterEmpty = BetterIterable.better(emptyList);
        
        BetterList<String> strings = betterEmpty.filter(String.class);
        assertEquals(0, strings.size());
    }

    @Test
    void testFilterByTypeWithNullValues() {
        List<Object> withNulls = Arrays.asList("hello", null, 42, null, "world");
        BetterIterable<Object> betterWithNulls = BetterIterable.better(withNulls);
        
        BetterList<String> strings = betterWithNulls.filter(String.class);
        assertEquals(Arrays.asList("hello", "world"), strings);
        
        BetterList<Integer> integers = betterWithNulls.filter(Integer.class);
        assertEquals(Arrays.asList(42), integers);
    }

    @Test
    void testFlatMap() {
        List<String> sentences = Arrays.asList("hello world", "java programming", "test case");
        BetterIterable<String> betterSentences = BetterIterable.better(sentences);

        BetterList<String> words = betterSentences.flatMap(s -> Arrays.asList(s.split(" "))).toList();
        assertEquals(Arrays.asList("hello", "world", "java", "programming", "test", "case"), words);

        List<List<Integer>> nestedNumbers = Arrays.asList(
                Arrays.asList(1, 2, 3),
                Arrays.asList(4, 5),
                Arrays.asList(6, 7, 8, 9)
        );
        BetterIterable<List<Integer>> betterNested = BetterIterable.better(nestedNumbers);

        BetterList<Integer> flattened = betterNested.flatMap(list -> list).toList();
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9), flattened);
    }

    @Test
    void testFlatMapWithEmptyIterables() {
        List<List<String>> listsWithEmpties = Arrays.asList(
                Arrays.asList("a", "b"),
                Arrays.asList(),
                Arrays.asList("c"),
                Arrays.asList(),
                Arrays.asList("d", "e")
        );
        BetterIterable<List<String>> betterLists = BetterIterable.better(listsWithEmpties);

        BetterList<String> flattened = betterLists.flatMap(list -> list).toList();
        assertEquals(Arrays.asList("a", "b", "c", "d", "e"), flattened);
    }

    @Test
    void testFlatMapWithSingleElements() {
        List<String> words = Arrays.asList("cat", "dog", "bird");
        BetterIterable<String> betterWords = BetterIterable.better(words);

        BetterList<Character> characters = betterWords.flatMap(word -> {
            List<Character> chars = new ArrayList<>();
            for (char c : word.toCharArray()) {
                chars.add(c);
            }
            return chars;
        }).toList();

        assertEquals(Arrays.asList('c', 'a', 't', 'd', 'o', 'g', 'b', 'i', 'r', 'd'), characters);
    }

    @Test
    void testFlatMapWithEmptySource() {
        List<String> emptyList = Arrays.asList();
        BetterIterable<String> betterEmpty = BetterIterable.better(emptyList);

        BetterList<Character> result = betterEmpty.flatMap(s -> {
            List<Character> chars = new ArrayList<>();
            for (char c : s.toCharArray()) {
                chars.add(c);
            }
            return chars;
        }).toList();

        assertEquals(0, result.size());
    }

    @Test
    void testChainedOperationsWithFilterByTypeAndFlatMap() {
        List<Object> mixedData = Arrays.asList(
                "hello world",
                42,
                "java programming",
                Arrays.asList(1, 2, 3),
                "test case"
        );
        BetterIterable<Object> betterMixed = BetterIterable.better(mixedData);

        BetterList<String> words = betterMixed
            .filter(String.class)
            .flatMap(s -> Arrays.asList(s.split(" ")));
        
        assertEquals(Arrays.asList("hello", "world", "java", "programming", "test", "case"), words);

        BetterList<String> uppercaseWords = betterMixed
            .filter(String.class)
            .flatMap(s -> Arrays.asList(s.split(" ")))
            .map(String::toUpperCase);
        
        assertEquals(Arrays.asList("HELLO", "WORLD", "JAVA", "PROGRAMMING", "TEST", "CASE"), uppercaseWords);
    }

    @Test
    void testFlatMapWithComplexTransformations() {
        List<Integer> numbers = Arrays.asList(2, 3, 4);
        BetterIterable<Integer> betterNumbers = BetterIterable.better(numbers);

        BetterList<Integer> ranges = betterNumbers.flatMap(n -> {
            List<Integer> range = new ArrayList<>();
            for (int i = 1; i <= n; i++) {
                range.add(i);
            }
            return range;
        }).toList();

        assertEquals(Arrays.asList(1, 2, 1, 2, 3, 1, 2, 3, 4), ranges);
        
        BetterMap<Integer, BetterList<Integer>> grouped = ranges.groupBy(Function.identity());
        assertEquals(Arrays.asList(1, 1, 1), grouped.get(1));
        assertEquals(Arrays.asList(2, 2, 2), grouped.get(2));
        assertEquals(Arrays.asList(3, 3), grouped.get(3));
        assertEquals(Arrays.asList(4), grouped.get(4));
    }
}