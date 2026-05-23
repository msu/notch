package edu.montana.notch.statements;

import edu.montana.notch.util.BetterList;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotchForLoopTest {

    @Test
    void testBasicForLoopWithString() {
        String result = exec("""
                for x in 'hello'
                    print(x)
                end
                """);
        assertEquals("h\ne\nl\nl\no\n", result);
    }

    @Test
    void testForLoopWithStringAndIndex() {
        String result = exec("""
                for x in 'abc' index i
                    print(i)
                    print(x)
                end
                """);
        assertEquals("0\na\n1\nb\n2\nc\n", result);
    }

    @Test
    void testForLoopWithEmptyString() {
        String result = exec("""
                for x in ''
                    print(x)
                end
                """);
        assertEquals("", result);
    }

    @Test
    void testForLoopWithSingleCharString() {
        String result = exec("""
                for x in 'x'
                    print(x)
                end
                """);
        assertEquals("x\n", result);
    }

    @Test
    void testForLoopScopeIsolation() {
        String result = exec("""
                for x in 'ab'
                    print(x)
                end
                print(x)
                """);
        assertEquals("a\nb\n<undefined>\n", result);
    }

    @Test
    void testForLoopIndexScopeIsolation() {
        String result = exec("""
                for x in 'ab' index i
                    print(i)
                end
                print(i)
                """);
        assertEquals("0\n1\n<undefined>\n", result);
    }

    @Test
    void testForLoopWithList() {
        BetterList<String> items = new BetterList<>(Arrays.asList("apple", "banana", "cherry"));
        String result = exec("""
                for item in items
                    print(item)
                end
                """, "items", items);
        assertEquals("apple\nbanana\ncherry\n", result);
    }

    @Test
    void testForLoopWithListAndIndex() {
        BetterList<Integer> numbers = new BetterList<>(Arrays.asList(10, 20, 30));
        String result = exec("""
                for num in numbers index i
                    print(i)
                    print(num)
                end
                """, "numbers", numbers);
        assertEquals("0\n10\n1\n20\n2\n30\n", result);
    }

    @Test
    void testForLoopWithArray() {
        String[] array = {"red", "green", "blue"};
        String result = exec("""
                for color in colors
                    print(color)
                end
                """, "colors", array);
        assertEquals("red\ngreen\nblue\n", result);
    }

    @Test
    void testForLoopWithArrayAndIndex() {
        Integer[] numbers = {100, 200, 300, 400};
        String result = exec("""
                for num in numbers index idx
                    print(idx)
                    print(num)
                end
                """, "numbers", numbers);
        assertEquals("0\n100\n1\n200\n2\n300\n3\n400\n", result);
    }

    @Test
    void testForLoopWithJavaList() {
        List<String> javaList = Arrays.asList("first", "second", "third");
        String result = exec("""
                for item in list
                    print(item)
                end
                """, "list", javaList);
        assertEquals("first\nsecond\nthird\n", result);
    }

    @Test
    void testNestedForLoops() {
        String result = exec("""
                for x in 'ab'
                    for y in 'cd'
                        print(x)
                        print(y)
                    end
                end
                """);
        assertEquals("a\nc\na\nd\nb\nc\nb\nd\n", result);
    }

    @Test
    void testNestedForLoopsWithIndices() {
        String result = exec("""
                for x in 'ab' index i
                    for y in 'cd' index j
                        print(i)
                        print(j)
                    end
                end
                """);
        assertEquals("0\n0\n0\n1\n1\n0\n1\n1\n", result);
    }

    @Test
    void testForLoopWithComplexExpression() {
        String result = exec("""
                for x in 'hi'
                    print('Value: ' + x)
                end
                """);
        assertEquals("Value: h\nValue: i\n", result);
    }

    @Test
    void testForLoopModifyingVariables() {
        String result = exec("""
                count = 0
                for x in 'abc'
                    count = count + 1
                end
                print(count)
                """);
        assertEquals("3\n", result);
    }

    @Test
    void testForLoopWithVariableAccumulation() {
        String result = exec("""
                result = ''
                for x in 'test'
                    result = result + x
                end
                print(result)
                """);
        assertEquals("test\n", result);
    }

    @Test
    void testForLoopIndexAccumulation() {
        String result = exec("""
                sum = 0
                for x in 'abcd' index i
                    sum = sum + i
                end
                print(sum)
                """);
        assertEquals("6\n", result);
    }

    @Test
    void testForLoopWithConditional() {
        String result = exec("""
                for x in 'hello'
                    if x == 'l'
                        print('found L')
                    else
                        print(x)
                    end
                end
                """);
        assertEquals("h\ne\nfound L\nfound L\no\n", result);
    }

    @Test
    void testForLoopWithIndexConditional() {
        String result = exec("""
                for x in 'hello' index i
                    if i == 0
                        print(x)
                    end
                end
                """);
        assertEquals("h\n", result);
    }

    @Test
    void testForLoopBreakingOutOfScope() {
        String result = exec("""
                outer = 'set'
                for x in 'ab'
                    inner = x
                end
                print(outer)
                print(inner)
                """);
        assertEquals("set\n<undefined>\n", result);
    }

    @Test
    void testForLoopWithNumericString() {
        String result = exec("""
                for digit in '123'
                    print(digit)
                end
                """);
        assertEquals("1\n2\n3\n", result);
    }

    @Test
    void testForLoopWithListOfNumbers() {
        String result = exec("""
                for digit in [1, 2, 3]
                    print(digit)
                end
                """);
        assertEquals("1\n2\n3\n", result);
    }

    @Test
    void testForLoopWithSpecialCharacters() {
        String result = exec("""
                for char in '!@#'
                    print(char)
                end
                """);
        assertEquals("!\n@\n#\n", result);
    }

    @Test
    void testForLoopWithUnicodeString() {
        String result = exec("""
                for char in 'αβγ'
                    print(char)
                end
                """);
        assertEquals("α\nβ\nγ\n", result);
    }

    @Test
    void testForLoopWithWhitespaceString() {
        String result = exec("""
                for char in ' \\t\\n'
                    print('char')
                end
                """);
        assertEquals("char\nchar\nchar\n", result);
    }

    @Test
    void testMultipleForLoopsSequentially() {
        String result = exec("""
                for x in 'ab'
                    print(x)
                end
                for y in 'cd'
                    print(y)
                end
                """);
        assertEquals("a\nb\nc\nd\n", result);
    }

    @Test
    void testForLoopWithMixedDataTypes() {
        BetterList<Object> mixedList = new BetterList<>(Arrays.asList("text", 42, true));
        String result = exec("""
                for item in mixed
                    print(item)
                end
                """, "mixed", mixedList);
        assertEquals("text\n42\ntrue\n", result);
    }

    @Test
    void testForLoopWithEmptyList() {
        BetterList<String> emptyList = new BetterList<>();
        String result = exec("""
                for item in empty
                    print(item)
                end
                """, "empty", emptyList);
        assertEquals("", result);
    }

    @Test
    void testForLoopWithSingleItemList() {
        BetterList<String> singleList = new BetterList<>(Arrays.asList("only"));
        String result = exec("""
                for item in single
                    print(item)
                end
                """, "single", singleList);
        assertEquals("only\n", result);
    }

    @Test
    void testForLoopIndexStartsAtZero() {
        String result = exec("""
                for x in 'test' index i
                    if i == 0
                        print('first')
                    end
                end
                """);
        assertEquals("first\n", result);
    }

    @Test
    void testForLoopIndexIncrementsCorrectly() {
        String result = exec("""
                for x in 'abcde' index i
                    if i == 4
                        print('last')
                    end
                end
                """);
        assertEquals("last\n", result);
    }
}