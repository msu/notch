package edu.montana.notch.statements;

import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.exec;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotchIndexAssignmentTest {

    @Test
    void testBasicListIndexAssignment() {
        String result = exec("""
                nums = [1, 2, 3]
                nums[0] = 10
                print(nums[0])
                """);
        assertEquals("10\n", result);
    }

    @Test
    void testListIndexAssignmentMiddleElement() {
        String result = exec("""
                nums = [1, 2, 3]
                nums[1] = 20
                print(nums[0])
                print(nums[1])
                print(nums[2])
                """);
        assertEquals("1\n20\n3\n", result);
    }

    @Test
    void testListIndexAssignmentLastElement() {
        String result = exec("""
                nums = [1, 2, 3]
                nums[2] = 30
                print(nums[2])
                """);
        assertEquals("30\n", result);
    }

    @Test
    void testIndexAssignmentWithVariableIndex() {
        String result = exec("""
                nums = [1, 2, 3]
                i = 1
                nums[i] = 99
                print(nums[1])
                """);
        assertEquals("99\n", result);
    }

    @Test
    void testIndexAssignmentWithExpressionIndex() {
        String result = exec("""
                nums = [1, 2, 3]
                nums[1 + 1] = 42
                print(nums[2])
                """);
        assertEquals("42\n", result);
    }

    @Test
    void testIndexAssignmentWithExpressionValue() {
        String result = exec("""
                nums = [1, 2, 3]
                nums[0] = nums[1] + nums[2]
                print(nums[0])
                """);
        assertEquals("5\n", result);
    }

    @Test
    void testIndexAssignmentWithStringValue() {
        String result = exec("""
                words = ['apple', 'banana', 'cherry']
                words[1] = 'blueberry'
                print(words[1])
                """);
        assertEquals("blueberry\n", result);
    }

    @Test
    void testMapIndexAssignmentExistingKey() {
        String result = exec("""
                scores = {'alice' -> 90, 'bob' -> 80}
                scores['bob'] = 85
                print(scores['bob'])
                """);
        assertEquals("85\n", result);
    }

    @Test
    void testMapIndexAssignmentNewKey() {
        String result = exec("""
                scores = {'alice' -> 90}
                scores['carol'] = 70
                print(scores['alice'])
                print(scores['carol'])
                """);
        assertEquals("90\n70\n", result);
    }

    @Test
    void testMapIndexAssignmentWithVariableKey() {
        String result = exec("""
                scores = {'alice' -> 90}
                key = 'alice'
                scores[key] = 95
                print(scores['alice'])
                """);
        assertEquals("95\n", result);
    }

    @Test
    void testNestedListIndexAssignment() {
        String result = exec("""
                grid = [[1, 2], [3, 4]]
                grid[0][1] = 9
                print(grid[0][1])
                print(grid[1][0])
                """);
        assertEquals("9\n3\n", result);
    }

    @Test
    void testIndexAssignmentInsideForLoop() {
        String result = exec("""
                nums = [1, 2, 3]
                for n in nums index i
                    nums[i] = n * 2
                end
                print(nums[0])
                print(nums[1])
                print(nums[2])
                """);
        assertEquals("2\n4\n6\n", result);
    }

    @Test
    void testIndexAssignmentInsideIf() {
        String result = exec("""
                nums = [1, 2, 3]
                if true
                    nums[0] = 100
                end
                print(nums[0])
                """);
        assertEquals("100\n", result);
    }

    @Test
    void testMultipleIndexAssignments() {
        String result = exec("""
                nums = [0, 0, 0]
                nums[0] = 1
                nums[1] = 2
                nums[2] = 3
                print(nums[0])
                print(nums[1])
                print(nums[2])
                """);
        assertEquals("1\n2\n3\n", result);
    }

    @Test
    void testIndexAssignmentDoesNotAffectOtherElements() {
        String result = exec("""
                nums = [1, 2, 3]
                nums[1] = 99
                print(nums[0])
                print(nums[2])
                """);
        assertEquals("1\n3\n", result);
    }
}
