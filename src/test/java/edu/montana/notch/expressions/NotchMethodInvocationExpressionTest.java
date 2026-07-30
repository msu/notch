package edu.montana.notch.expressions;

import edu.montana.notch.types.TypeSystem;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static edu.montana.notch.NotchTestUtils.eval;
import static edu.montana.notch.NotchTestUtils.evalNoCatch;
import static edu.montana.notch.NotchTestUtils.exec;
import static edu.montana.notch.runtime.NotchRuntime.UNDEFINED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NotchMethodInvocationExpressionTest {

    @Test
    public void testMethodInvocationWorks() {
        Object result = eval("foo.instanceMethod()", "foo", new SampleClass());
        assertEquals("instanceMethod", result);
    }

    @Test
    public void testStaticMethodInvocationWorks() {
        var type = TypeSystem.getType(SampleClass.class);
        Object result = eval("SampleClass.staticMethod()", type.getSimpleName(), type);
        assertEquals("staticMethod", result);
    }

    @Test
    public void testMethodWithSingleArgument() {
        Object result = eval("foo.withArg('test')", "foo", new SampleClass());
        assertEquals("test", result);
    }

    @Test
    public void testMethodWithMultipleArguments() {
        Object result = eval("foo.withTwoArgs(1, 2)", "foo", new SampleClass());
        assertEquals(3, result);
    }

    @Test
    public void testPropertyAccess() {
        Object result = eval("foo.instanceProperty", "foo", new SampleClass());
        assertEquals("instanceProperty", result);
    }

    @Test
    public void testNullPropertyAccessIsNullSafe() {
        Object result = eval("foo.instanceProperty", "foo", null);
        assertEquals(UNDEFINED, result);
    }

    @Test
    public void testChainedMethodCalls() {
        Object result = eval("foo.getChild().instanceMethod()", "foo", new SampleClass());
        assertEquals("instanceMethod", result);
    }

    @Test
    public void testChainedPropertyAccess() {
        Object result = eval("foo.child.instanceProperty", "foo", new SampleClass());
        assertEquals("instanceProperty", result);
    }

    @Test
    public void testOverloadedMethodDispatchesCorrectly() {
        Object result = eval("foo.overloadedMethod(x)", "foo", new SampleClass(), "x", 1);
        assertEquals(1, result);

        result = eval("foo.overloadedMethod(x)", "foo", new SampleClass(), "x", 1L);
        assertEquals(1L, result);

        result = eval("foo.overloadedMethod(x)", "foo", new SampleClass(), "x", "foo");
        assertEquals("foo", result);
    }

    @Test
    public void testNullPropertyInChainReturnsNull() {
        Object result = eval("foo.get_null.instanceProperty", "foo", new SampleClass());
        assertEquals(UNDEFINED, result);
    }

    @Test
    public void testMethodWithNullParameter() {
        Object result = eval("foo.withNullableArg(x)", "foo", new SampleClass(), "x", null);
        assertEquals("null", result);
    }

    @Test
    public void testMethodWithNonNullParameter() {
        Object result = eval("foo.withNullableArg(x)", "foo", new SampleClass(), "x", "test");
        assertEquals("test", result);
    }

    @Test
    public void testExceptionsPropagateProperly() {
        assertThrows(CustomException.class, () -> evalNoCatch("foo.throwsException(x)", "foo", new SampleClass(), "x", 10));
    }

    @Test
    public void testConstructorViaTypeInvocation() {
        var type = TypeSystem.getType(SampleClass.class);
        Object result = eval("SampleClass()", type.getSimpleName(), type);
        assertInstanceOf(SampleClass.class, result);
    }

    @Test
    public void testJvmClassConstructor() {
        assertInstanceOf(java.util.ArrayList.class, eval("java.util.ArrayList()"));
    }

    @Test
    public void testConstructorWithArgument() {
        assertEquals("hi", eval("java.lang.StringBuilder('hi').toString()"));
    }

    @Test
    public void testConstructorWithNoMatchingOverloadReports() {
        var ex = assertThrows(RuntimeException.class, () -> eval("java.util.List()"));
        assertTrue(ex.getMessage().contains("no constructor"), ex.getMessage());
    }

    @Test
    public void bareThrowableInvocationErrorsWithoutImport() {
        var ex = assertThrows(RuntimeException.class, () -> eval("IOException('boom')"));
        String msg = ex.getMessage();
        assertTrue(msg.contains("undefined function 'IOException'"), msg);
        assertTrue(msg.contains("import java.io.IOException"), msg);
        assertTrue(msg.contains("new IOException(...)"), msg);
    }

    @Test
    public void bareNonThrowableInvocationSuggestsImportButNotNew() {
        var ex = assertThrows(RuntimeException.class, () -> eval("ArrayList('x')"));
        String msg = ex.getMessage();
        assertTrue(msg.contains("undefined function 'ArrayList'"), msg);
        assertTrue(msg.contains("import java.util.ArrayList"), msg);
        assertTrue(!msg.contains("new ArrayList(...)"), msg);
    }

    @Test
    public void boundThrowableTypeConstructsOnInvocation() {
        var type = TypeSystem.getType(java.io.IOException.class);
        Object result = eval("IOException('boom')", "IOException", type);
        assertInstanceOf(java.io.IOException.class, result);
    }

    @Test
    public void newOnJvmTypeSuggestsImport() {
        // `new ArrayList('x')` without an import should point at the import
        var ex = assertThrows(RuntimeException.class, () -> eval("new ArrayList('x')"));
        String msg = ex.getMessage();
        assertTrue(msg.contains("no class named 'ArrayList'"), msg);
        assertTrue(msg.contains("import java.util.ArrayList"), msg);
        //TODO: Remove following Once a error handler class
        // (new test suit will be needed for these checks)
        assertTrue(!msg.contains("make sure the class is declared"), msg);
    }

    @Test
    public void newUndefinedGuidanceDeclareClass() {
        var ex = assertThrows(RuntimeException.class, () -> eval("new Nonexistent999()"));
        String msg = ex.getMessage();
        assertTrue(msg.contains("no class named 'Nonexistent999'"), msg);
        assertTrue(msg.contains("make sure the class is declared"), msg);
    }

    @Test
    public void newOnPrimitiveDoesNotSuggestImportingIt() {
        var ex = assertThrows(RuntimeException.class, () -> eval("new int()"));
        String msg = ex.getMessage();
        assertTrue(msg.contains("no class named 'int'"), msg);
        assertTrue(!msg.contains("import int"), msg);
    }

    @Test
    public void constructorBodyExceptionSurfacesRealCause() {
        var type = TypeSystem.getType(java.io.FileReader.class);
        var ex = assertThrows(RuntimeException.class, () -> exec("err = new FileReader('/no/such/file')\n", "FileReader", type));
        String msg = ex.getMessage();
        assertTrue(msg.contains("FileNotFoundException") || msg.contains("/no/such/file"), msg);
        assertTrue(!msg.contains("InvocationTargetException"), msg);
    }

    @Test
    public void errorEvaluatingConstructorArgIsNotReportedAsNoConstructor() {
        // (closure invoked with the wrong arity)
        // must surface as its own error, not swallowed and mislabeled
        String script = """
                dbl = \\ x -> x * 2
                err = new IOException(dbl(1, 2, 3))
                """;
        var ex = assertThrows(RuntimeException.class, () -> exec(script));
        String msg = ex.getMessage();
        assertTrue(msg.contains("does not match the number of parameters"), msg);
        assertTrue(!msg.contains("no constructor"), msg);
    }

    @Test
    public void catchingThrowableDoesNotMakeBareNameConstructibleLater() {
        String script = """
                try
                  throw IOException('first')
                catch IOException as e
                  print('caught')
                end
                after = IOException('boom')
                """;
        var ex = assertThrows(RuntimeException.class, () -> exec(script));
        var msg = ex.getMessage();
        assertTrue(msg.contains("undefined function 'IOException'"), ex.getMessage());
    }

    public static class SampleClass {

        public static String staticMethod() {
            return "staticMethod";
        }

        public String instanceMethod() {
            return "instanceMethod";
        }

        public static String staticProperty() {
            return "staticProperty";
        }

        public String instanceProperty() {
            return "instanceProperty";
        }

        public String withArg(String arg) {
            return arg;
        }

        public int withTwoArgs(int a, int b) {
            return a + b;
        }

        public String overloadedMethod(String s) {
            return s;
        }

        public int overloadedMethod(int s) {
            return s;
        }

        public Integer overloadedMethod(Integer s) {
            return s;
        }

        public Long overloadedMethod(Long s) {
            return s;
        }

        public SampleClass getChild() {
            return new SampleClass();
        }

        public SampleClass getNull() {
            return null;
        }

        public String withNullableArg(String arg) {
            return arg == null ? "null" : arg;
        }

        public void throwsException(int depth) {
            if (depth <= 0) {
                throw new CustomException();
            } else {
                throwsException(depth - 1);
            }
        }
    }

    public static class CustomException extends RuntimeException {
    }
}
