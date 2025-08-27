package bigsky.notch;

import bigsky.notch.expr.NotchExpression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static bigsky.notch.NotchTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Notch Parser Integration Tests")
public class NotchParserIntegrationTest {

    @Test
    @DisplayName("Basic expression evaluation")
    public void testBasicExpressions() {
        assertEquals(42, eval("42"));
        assertEquals(true, eval("true"));
        assertEquals(false, eval("false"));
        assertEquals("hello", eval("'hello'"));
        assertEquals("hello", eval("\"hello\""));
        assertEquals("hello", eval(":hello"));
    }

    @Test
    @DisplayName("String escape sequences")
    public void testStringEscapes() {
        assertEquals("hello\nworld", eval("'hello\\nworld'"));
        assertEquals("hello\rworld", eval("'hello\\rworld'"));
        assertEquals("hello\\world", eval("'hello\\\\world'"));
        assertEquals("hello'world", eval("'hello\\'world'"));
        assertEquals("hello\"world", eval("\"hello\\\"world\""));
    }

    @Test
    @DisplayName("Equality expressions")
    public void testEqualityExpressions() {
        assertEquals(true, eval("5 == 5"));
        assertEquals(false, eval("5 == 3"));
        assertEquals(true, eval("'hello' == 'hello'"));
        assertEquals(false, eval("'hello' == 'world'"));
        assertEquals(true, eval("true == true"));
        assertEquals(false, eval("true == false"));
    }

    @Test
    @DisplayName("Fallback expressions")
    public void testFallbackExpressions() {
        NotchParser parser = new NotchParser("foo ?: 'default'");
        NotchExpression notchExpression = parser.parseExpression();
        Object result = notchExpression.evaluate(Map.of("foo", "value"));
        assertEquals("value", result);
        
        result = notchExpression.evaluate(Map.of());
        assertEquals("default", result);
    }

    @Test
    @DisplayName("Conditional expressions")
    public void testConditionalExpressions() {
        assertEquals("yes", eval("'yes' if true"));
        assertEquals("<undefined>", eval("'yes' if false").toString());
        assertEquals("no", eval("'yes' if false else 'no'"));
        assertEquals("yes", eval("'yes' if true else 'no'"));
    }

    @Test
    @DisplayName("Complex nested expressions")
    public void testNestedExpressions() {
        assertEquals("found", eval("'found' if 5 == 5 else 'not found'"));
        assertEquals("not found", eval("'found' if 5 == 3 else 'not found'"));
        assertEquals("backup", eval("'primary' if false else 'secondary' if false else 'backup'"));
        assertEquals("default", eval("false ?: false ?: 'default'"));
    }

    @Test
    @DisplayName("Print statement execution")
    public void testPrintStatements() {
        assertEquals("42\n", exec("print(42)"));
        assertEquals("hello world\n", exec("print('hello world')"));
        assertEquals("true\n", exec("print(true)"));
        assertEquals("false\n", exec("print(false)"));
    }

    @Test
    @DisplayName("Multiple print statements")
    public void testMultiplePrintStatements() {
        assertEquals("hello\nworld\n", exec("print('hello') print('world')"));
        assertEquals("1\n2\n3\n", exec("print(1) print(2) print(3)"));
    }

    @Test
    @DisplayName("If statement execution")
    public void testIfStatements() {
        assertEquals("true branch\n", exec("if true print('true branch') end"));
        assertEquals("", exec("if false print('true branch') end"));
        assertEquals("false branch\n", exec("if false print('true branch') else print('false branch') end"));
        assertEquals("true branch\n", exec("if true print('true branch') else print('false branch') end"));
    }

    @Test
    @DisplayName("Nested if statements")
    public void testNestedIfStatements() {
        String program = """
            if true
                if true
                    print('nested true')
                else
                    print('nested false')
                end
            else
                print('outer false')
            end
            """;
        assertEquals("nested true\n", exec(program));
        
        program = """
            if true
                if false
                    print('nested true')
                else
                    print('nested false')
                end
            else
                print('outer false')
            end
            """;
        assertEquals("nested false\n", exec(program));
    }

    @Test
    @DisplayName("For loop with string iteration")
    public void testForLoopString() {
        assertEquals("h\ne\nl\nl\no\n", exec("for c in 'hello' print(c) end"));
        assertEquals("a\nb\nc\n", exec("for char in 'abc' print(char) end"));
    }

    @Test
    @DisplayName("For loop with index")
    public void testForLoopWithIndex() {
        assertEquals("0\nh\n1\ne\n2\nl\n3\nl\n4\no\n",
                    exec("for c in 'hello' index i print(i) print(c) end"));
        
        assertEquals("0\n1\n2\n", exec("for c in 'abc' index i print(i) end"));
    }

    @Test
    @DisplayName("For loop variable scoping")
    public void testForLoopScoping() {
        String result = exec("for x in 'ab' print(x) end print(x)");
        assertTrue(result.contains("<undefined>"));
        
        result = exec("for x in 'ab' index i print(i) end print(i)");
        assertTrue(result.contains("<undefined>"));
    }

    @Test
    @DisplayName("Complex for loop with conditions")
    public void testComplexForLoop() {
        String program = """
            for c in 'hello'
                if c == 'l'
                    print('found l')
                else
                    print(c)
                end
            end
            """;
        assertEquals("h\ne\nfound l\nfound l\no\n", exec(program));
    }

    @Test
    @DisplayName("Mixed statements program")
    public void testMixedProgram() {
        String program = """
            print('Starting program')
            if true
                print('Condition met')
                for c in 'hi'
                    print(c)
                end
            else
                print('Condition not met')
            end
            print('Program complete')
            """;
        assertEquals("Starting program\nCondition met\nh\ni\nProgram complete\n", exec(program));
    }

    @Test
    @DisplayName("Expression evaluation with variables")
    public void testExpressionWithVariables() {
        NotchParser parser = new NotchParser("name == 'Alice'");
        NotchExpression expr = parser.parseExpression();
        Object result = expr.evaluate(Map.of("name", "Alice"));
        assertEquals(true, result);
        
        result = expr.evaluate(Map.of("name", "Bob"));
        assertEquals(false, result);
    }

    @Test
    @DisplayName("Complex program with variables and logic")
    public void testComplexProgramWithVariables() {
        NotchParser parser = new NotchParser("""
            if user == 'admin'
                print('Admin access granted')
                for item in items
                    print(item)
                end
            else
                print('Regular user access')
            end
            """);
        
        StringBuilder output = new StringBuilder();
        bigsky.notch.runtime.NotchRuntime runtime = new bigsky.notch.runtime.NotchRuntime(
            Map.of("user", "admin", "items", "abc")
        );
        runtime.setOut(obj -> output.append(obj).append("\n"));
        
        parser.parse().execute(runtime);
        assertEquals("Admin access granted\na\nb\nc\n", output.toString());
    }

    @Test
    @DisplayName("Error handling for malformed syntax")
    public void testErrorHandling() {
        assertThrows(ParseException.class, () -> {
            new NotchParser("print(").parse();
        });
        
        assertThrows(ParseException.class, () -> {
            new NotchParser("if true print('test')").parse(); // missing 'end'
        });
        
        assertThrows(ParseException.class, () -> {
            new NotchParser("for x print(x) end").parse(); // missing 'in'
        });
        
        assertThrows(ParseException.class, () -> {
            new NotchParser("'unterminated string").parseExpression();
        });
    }

    @Test
    @DisplayName("Empty and whitespace-only programs")
    public void testEmptyPrograms() {
        assertEquals("", exec(""));
        assertEquals("", exec("   "));
        assertEquals("", exec("\n\n\n"));
        assertEquals("", exec("   \n  \n  "));
    }

    @Test
    @DisplayName("Terse string syntax")
    public void testTerseStrings() {
        assertEquals("hello", eval(":hello"));
        assertEquals("test123", eval(":test123"));
        assertEquals("a", eval(":a"));
    }

    @Test
    @DisplayName("Boolean logic in conditions")
    public void testBooleanLogic() {
        assertEquals("true\n", exec("if true == true print('true') else print('false') end"));
        assertEquals("false\n", exec("if true == false print('true') else print('false') end"));
        assertEquals("equal\n", exec("if 5 == 5 print('equal') else print('not equal') end"));
        assertEquals("not equal\n", exec("if 5 == 3 print('equal') else print('not equal') end"));
    }
}