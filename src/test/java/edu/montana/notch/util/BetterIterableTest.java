package edu.montana.notch.util;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

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
        
        BetterList<String> mapped = betterNumbers.map(String::valueOf);
        assertEquals(5, mapped.size());
        assertEquals(Arrays.asList("1", "2", "3", "4", "5"), mapped);
        
        BetterList<Integer> doubled = betterNumbers.map(n -> n * 2);
        assertEquals(Arrays.asList(2, 4, 6, 8, 10), doubled);
    }

    @Test
    void testToSet() {
        List<String> withDuplicates = Arrays.asList("a", "b", "a", "c", "b");
        BetterIterable<String> betterIterable = BetterIterable.better(withDuplicates);
        
        Set<String> result = betterIterable.toSet();
        assertEquals(3, result.size());
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
        assertTrue(result.contains("c"));
        assertTrue(result instanceof LinkedHashSet);
    }

    @Test
    void testToList() {
        Set<String> sourceSet = new HashSet<>(Arrays.asList("x", "y", "z"));
        BetterIterable<String> betterIterable = BetterIterable.better(sourceSet);
        
        BetterList<String> result = betterIterable.toList();
        assertEquals(3, result.size());
        assertTrue(result.contains("x"));
        assertTrue(result.contains("y"));
        assertTrue(result.contains("z"));
    }

    @Test
    void testToMap() {
        List<String> words = Arrays.asList("cat", "dog", "car", "door", "cab");
        BetterIterable<String> betterWords = BetterIterable.better(words);
        
        Map<Integer, List<String>> byLength = betterWords.toMap(String::length);
        assertEquals(2, byLength.size());
        assertEquals(4, byLength.get(3).size());
        assertEquals(Arrays.asList("cat", "dog", "car", "cab"), byLength.get(3));
        assertEquals(Arrays.asList("door"), byLength.get(4));
        assertTrue(byLength instanceof LinkedHashMap);
    }

    @Test
    void testToOrderedMap() {
        List<String> words = Arrays.asList("zebra", "apple", "banana");
        BetterIterable<String> betterWords = BetterIterable.better(words);
        
        TreeMap<Character, List<String>> orderedByFirstChar = betterWords.toOrderedMap(s -> s.charAt(0));
        assertTrue(orderedByFirstChar instanceof TreeMap);
        assertEquals(3, orderedByFirstChar.size());
        
        List<Character> keys = new ArrayList<>(orderedByFirstChar.keySet());
        assertEquals(Arrays.asList('a', 'b', 'z'), keys);
    }

    @Test
    void testToOrderedMapWithComparator() {
        List<String> words = Arrays.asList("zebra", "apple", "banana");
        BetterIterable<String> betterWords = BetterIterable.better(words);
        
        Comparator<Character> reverseOrder = Comparator.<Character>naturalOrder().reversed();
        TreeMap<Character, List<String>> orderedByFirstChar = betterWords.toOrderedMap(s -> s.charAt(0), reverseOrder);
        
        assertTrue(orderedByFirstChar instanceof TreeMap);
        List<Character> keys = new ArrayList<>(orderedByFirstChar.keySet());
        assertEquals(Arrays.asList('z', 'b', 'a'), keys);
    }

    @Test
    void testToDistinctMap() {
        List<String> words = Arrays.asList("cat", "car", "dog", "door");
        BetterIterable<String> betterWords = BetterIterable.better(words);
        
        Map<Character, String> distinctByFirstChar = betterWords.toDistinctMap(s -> s.charAt(0));
        assertEquals(2, distinctByFirstChar.size());
        assertEquals("car", distinctByFirstChar.get('c'));
        assertEquals("door", distinctByFirstChar.get('d'));
    }

    @Test
    void testToOrderedDistinctMap() {
        List<String> words = Arrays.asList("zebra", "apple", "banana");
        BetterIterable<String> betterWords = BetterIterable.better(words);
        
        TreeMap<Character, String> orderedDistinct = betterWords.toOrderedDistinctMap(s -> s.charAt(0));
        assertTrue(orderedDistinct instanceof TreeMap);
        assertEquals(3, orderedDistinct.size());
        
        List<Character> keys = new ArrayList<>(orderedDistinct.keySet());
        assertEquals(Arrays.asList('a', 'b', 'z'), keys);
    }

    @Test
    void testToOrderedDistinctMapWithComparator() {
        List<String> words = Arrays.asList("zebra", "apple", "banana");
        BetterIterable<String> betterWords = BetterIterable.better(words);
        
        Comparator<Character> reverseOrder = Comparator.<Character>naturalOrder().reversed();
        TreeMap<Character, String> orderedDistinct = betterWords.toOrderedDistinctMap(s -> s.charAt(0), reverseOrder);
        
        assertTrue(orderedDistinct instanceof TreeMap);
        List<Character> keys = new ArrayList<>(orderedDistinct.keySet());
        assertEquals(Arrays.asList('z', 'b', 'a'), keys);
    }

    @Test
    void testFilter() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        BetterIterable<Integer> betterNumbers = BetterIterable.better(numbers);
        
        BetterList<Integer> evenNumbers = betterNumbers.filter(n -> n % 2 == 0);
        assertEquals(Arrays.asList(2, 4, 6, 8, 10), evenNumbers);
        
        BetterList<Integer> greaterThanFive = betterNumbers.filter(n -> n > 5);
        assertEquals(Arrays.asList(6, 7, 8, 9, 10), greaterThanFive);
        
        BetterList<Integer> noMatches = betterNumbers.filter(n -> n > 20);
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
        
        assertEquals("apple, banana, cherry", betterItems.join(", "));
        assertEquals("apple|banana|cherry", betterItems.join("|"));
        assertEquals("applebananacherry", betterItems.join(""));
        
        List<Integer> numbers = Arrays.asList(1, 2, 3);
        BetterIterable<Integer> betterNumbers = BetterIterable.better(numbers);
        assertEquals("1-2-3", betterNumbers.join("-"));
    }

    @Test
    void testJoinWithEmptyIterable() {
        List<String> emptyList = Arrays.asList();
        BetterIterable<String> betterEmpty = BetterIterable.better(emptyList);
        
        assertEquals("", betterEmpty.join(", "));
        assertEquals("", betterEmpty.join("|"));
    }

    @Test
    void testJoinWithSingleElement() {
        List<String> singleItem = Arrays.asList("alone");
        BetterIterable<String> betterSingle = BetterIterable.better(singleItem);
        
        assertEquals("alone", betterSingle.join(", "));
        assertEquals("alone", betterSingle.join("|"));
    }

    @Test
    void testChainedOperations() {
        List<String> words = Arrays.asList("apple", "banana", "apricot", "cherry", "avocado");
        BetterIterable<String> betterWords = BetterIterable.better(words);
        
        BetterList<String> result = betterWords
                .filter(s -> s.startsWith("a"))
                .map(String::toUpperCase);
        
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
        
        BetterList<String> mapped = fromSet.map(String::toUpperCase);
        assertEquals(3, mapped.size());
        assertTrue(mapped.contains("X"));
        assertTrue(mapped.contains("Y"));
        assertTrue(mapped.contains("Z"));
        
        Queue<Integer> sourceQueue = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        BetterIterable<Integer> fromQueue = BetterIterable.better(sourceQueue);
        
        String joined = fromQueue.join("-");
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
        
        BetterList<String> strings = betterMixed.filterByType(String.class);
        assertEquals(Arrays.asList("hello", "world", "test"), strings);
        
        BetterList<Integer> integers = betterMixed.filterByType(Integer.class);
        assertEquals(Arrays.asList(42, 100), integers);
        
        BetterList<Double> doubles = betterMixed.filterByType(Double.class);
        assertEquals(Arrays.asList(3.14), doubles);
        
        BetterList<Boolean> booleans = betterMixed.filterByType(Boolean.class);
        assertEquals(0, booleans.size());
    }

    @Test
    void testFilterByTypeWithInheritance() {
        List<Object> objects = Arrays.asList("string", new ArrayList<>(), new LinkedList<>(), 42);
        BetterIterable<Object> betterObjects = BetterIterable.better(objects);
        
        BetterList<List> lists = betterObjects.filterByType(List.class);
        assertEquals(2, lists.size());
        assertTrue(lists.get(0) instanceof ArrayList);
        assertTrue(lists.get(1) instanceof LinkedList);
        
        BetterList<ArrayList> arrayLists = betterObjects.filterByType(ArrayList.class);
        assertEquals(1, arrayLists.size());
        assertTrue(arrayLists.get(0) instanceof ArrayList);
    }

    @Test
    void testFilterByTypeWithEmptyIterable() {
        List<Object> emptyList = Arrays.asList();
        BetterIterable<Object> betterEmpty = BetterIterable.better(emptyList);
        
        BetterList<String> strings = betterEmpty.filterByType(String.class);
        assertEquals(0, strings.size());
    }

    @Test
    void testFilterByTypeWithNullValues() {
        List<Object> withNulls = Arrays.asList("hello", null, 42, null, "world");
        BetterIterable<Object> betterWithNulls = BetterIterable.better(withNulls);
        
        BetterList<String> strings = betterWithNulls.filterByType(String.class);
        assertEquals(Arrays.asList("hello", "world"), strings);
        
        BetterList<Integer> integers = betterWithNulls.filterByType(Integer.class);
        assertEquals(Arrays.asList(42), integers);
    }

    @Test
    void testFlatMap() {
        List<String> sentences = Arrays.asList("hello world", "java programming", "test case");
        BetterIterable<String> betterSentences = BetterIterable.better(sentences);
        
        BetterList<String> words = betterSentences.flatMap(s -> Arrays.asList(s.split(" ")));
        assertEquals(Arrays.asList("hello", "world", "java", "programming", "test", "case"), words);
        
        List<List<Integer>> nestedNumbers = Arrays.asList(
            Arrays.asList(1, 2, 3),
            Arrays.asList(4, 5),
            Arrays.asList(6, 7, 8, 9)
        );
        BetterIterable<List<Integer>> betterNested = BetterIterable.better(nestedNumbers);
        
        BetterList<Integer> flattened = betterNested.flatMap(list -> list);
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
        
        BetterList<String> flattened = betterLists.flatMap(list -> list);
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
        });
        
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
        });
        
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
            .filterByType(String.class)
            .flatMap(s -> Arrays.asList(s.split(" ")));
        
        assertEquals(Arrays.asList("hello", "world", "java", "programming", "test", "case"), words);
        
        BetterList<String> uppercaseWords = betterMixed
            .filterByType(String.class)
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
        });
        
        assertEquals(Arrays.asList(1, 2, 1, 2, 3, 1, 2, 3, 4), ranges);
        
        Map<Integer, List<Integer>> grouped = ranges.toMap(Function.identity());
        assertEquals(Arrays.asList(1, 1, 1), grouped.get(1));
        assertEquals(Arrays.asList(2, 2, 2), grouped.get(2));
        assertEquals(Arrays.asList(3, 3), grouped.get(3));
        assertEquals(Arrays.asList(4), grouped.get(4));
    }
}