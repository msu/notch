package edu.montana.notch.statements;

import edu.montana.notch.util.BetterList;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotchIfTest {

    @Test
    void testBasicIfTrue() {
        String result = exec("""
                if true
                    print('true branch')
                end
                """);
        assertEquals("true branch\n", result);
    }

    @Test
    void testBasicIfFalse() {
        String result = exec("""
                if false
                    print('true branch')
                end
                """);
        assertEquals("", result);
    }

    @Test
    void testIfElseTrue() {
        String result = exec("""
                if true
                    print('true branch')
                else
                    print('false branch')
                end
                """);
        assertEquals("true branch\n", result);
    }

    @Test
    void testIfElseFalse() {
        String result = exec("""
                if false
                    print('true branch')
                else
                    print('false branch')
                end
                """);
        assertEquals("false branch\n", result);
    }

    @Test
    void testIfWithVariableTrue() {
        String result = exec("""
                condition = true
                if condition
                    print('condition is true')
                end
                """);
        assertEquals("condition is true\n", result);
    }

    @Test
    void testIfWithVariableFalse() {
        String result = exec("""
                condition = false
                if condition
                    print('condition is true')
                else
                    print('condition is false')
                end
                """);
        assertEquals("condition is false\n", result);
    }

    @Test
    void testIfWithComparison() {
        String result = exec("""
                x = 5
                if x == 5
                    print('x equals 5')
                else
                    print('x does not equal 5')
                end
                """);
        assertEquals("x equals 5\n", result);
    }

    @Test
    void testIfWithInequalityComparison() {
        String result = exec("""
                x = 3
                if x != 5
                    print('x is not 5')
                else
                    print('x is 5')
                end
                """);
        assertEquals("x is not 5\n", result);
    }

    @Test
    void testIfWithStringComparison() {
        String result = exec("""
                name = 'Alice'
                if name == 'Alice'
                    print('Hello Alice')
                else
                    print('Hello stranger')
                end
                """);
        assertEquals("Hello Alice\n", result);
    }

    @Test
    void testIfWithMultipleStatements() {
        String result = exec("""
                if true
                    print('first')
                    print('second')
                    print('third')
                end
                """);
        assertEquals("first\nsecond\nthird\n", result);
    }

    @Test
    void testIfElseWithMultipleStatements() {
        String result = exec("""
                if false
                    print('true first')
                    print('true second')
                else
                    print('false first')
                    print('false second')
                end
                """);
        assertEquals("false first\nfalse second\n", result);
    }

    @Test
    void testNestedIf() {
        String result = exec("""
                x = 10
                if x > 5
                    if x > 8
                        print('x is greater than 8')
                    else
                        print('x is between 5 and 8')
                    end
                else
                    print('x is 5 or less')
                end
                """);
        assertEquals("x is greater than 8\n", result);
    }

    @Test
    void testNestedIfElse() {
        String result = exec("""
                x = 7
                if x > 5
                    if x > 8
                        print('x is greater than 8')
                    else
                        print('x is between 5 and 8')
                    end
                else
                    print('x is 5 or less')
                end
                """);
        assertEquals("x is between 5 and 8\n", result);
    }

    @Test
    void testIfModifyingVariables() {
        String result = exec("""
                result = 'initial'
                if true
                    result = 'modified'
                end
                print(result)
                """);
        assertEquals("modified\n", result);
    }

    @Test
    void testIfElseModifyingVariables() {
        String result = exec("""
                result = 'initial'
                if false
                    result = 'true branch'
                else
                    result = 'false branch'
                end
                print(result)
                """);
        assertEquals("false branch\n", result);
    }

    @Test
    void testIfWithComplexExpression() {
        String result = exec("""
                x = 5
                y = 3
                if x + y == 8
                    print('sum is 8')
                else
                    print('sum is not 8')
                end
                """);
        assertEquals("sum is 8\n", result);
    }

    @Test
    void testIfWithStringConcatenation() {
        String result = exec("""
                name = 'World'
                if name == 'World'
                    print('Hello ' + name)
                end
                """);
        assertEquals("Hello World\n", result);
    }

    @Test
    void testIfWithListAccess() {
        BetterList<String> items = new BetterList<>(Arrays.asList("apple", "banana", "cherry"));
        String result = exec("""
                if items.size == 3
                    print('list has 3 items')
                else
                    print('list does not have 3 items')
                end
                """, "items", items);
        assertEquals("list has 3 items\n", result);
    }

    @Test
    void testIfInsideForLoop() {
        String result = exec("""
                for x in 'abc'
                    if x == 'b'
                        print('found b')
                    else
                        print(x)
                    end
                end
                """);
        assertEquals("a\nfound b\nc\n", result);
    }

    @Test
    void testMultipleIfStatements() {
        String result = exec("""
                x = 5
                if x > 0
                    print('positive')
                end
                if x == 5
                    print('equals 5')
                end
                if x < 10
                    print('less than 10')
                end
                """);
        assertEquals("positive\nequals 5\nless than 10\n", result);
    }

    @Test
    void testIfWithBooleanLogic() {
        String result = exec("""
                a = true
                b = false
                if a and b
                    print('both true')
                else
                    print('not both true')
                end
                """);
        assertEquals("not both true\n", result);
    }

    @Test
    void testIfWithNegation() {
        String result = exec("""
                condition = false
                if not condition
                    print('condition is false')
                else
                    print('condition is true')
                end
                """);
        assertEquals("condition is false\n", result);
    }

    @Test
    void testIfScopeIsolation() {
        String result = exec("""
                if true
                    inner = 'inside if'
                end
                print(inner)
                """);
        assertEquals("inside if\n", result);
    }

    @Test
    void testIfElseScopeIsolation() {
        String result = exec("""
                if false
                    trueVar = 'true branch'
                else
                    falseVar = 'false branch'
                end
                print(trueVar)
                print(falseVar)
                """);
        assertEquals("<undefined>\nfalse branch\n", result);
    }

    @Test
    void testIfWithZeroComparison() {
        String result = exec("""
                x = 0
                if x == 0
                    print('x is zero')
                else
                    print('x is not zero')
                end
                """);
        assertEquals("x is zero\n", result);
    }

    @Test
    void testIfWithEmptyString() {
        String result = exec("""
                str = ''
                if str == ''
                    print('string is empty')
                else
                    print('string is not empty')
                end
                """);
        assertEquals("string is empty\n", result);
    }

    @Test
    void testIfWithNullComparison() {
        String result = exec("""
                value = null
                if value == null
                    print('value is null')
                else
                    print('value is not null')
                end
                """);
        assertEquals("value is null\n", result);
    }

    @Test
    void testDeepNestedIf() {
        String result = exec("""
                level = 3
                if level > 0
                    if level > 1
                        if level > 2
                            print('level 3')
                        else
                            print('level 2')
                        end
                    else
                        print('level 1')
                    end
                else
                    print('level 0')
                end
                """);
        assertEquals("level 3\n", result);
    }

    @Test
    void testIfWithMethodCall() {
        String result = exec("""
                text = 'hello'
                if text.length() == 5
                    print('text has 5 characters')
                else
                    print('text does not have 5 characters')
                end
                """);
        assertEquals("text has 5 characters\n", result);
    }

    @Test
    void testIfElseChain() {
        String result = exec("""
                grade = 85
                if grade >= 90
                    print('A')
                else
                    if grade >= 80
                        print('B')
                    else
                        if grade >= 70
                            print('C')
                        else
                            print('F')
                        end
                    end
                end
                """);
        assertEquals("B\n", result);
    }

    @Test
    void testIfWithVariableAssignmentInCondition() {
        String result = exec("""
                x = 10
                y = 5
                z = x + y
                if z > 10
                    print('sum is greater than 10: ' + z)
                else
                    print('sum is not greater than 10: ' + z)
                end
                """);
        assertEquals("sum is greater than 10: 15\n", result);
    }
}