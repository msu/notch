package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.types.TypeSystem;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static bigsky.notch.NotchTestUtils.eval;
import static bigsky.notch.NotchTestUtils.evalNoCatch;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NotchMethodInvocationTest {

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
        assertEquals(null, result);
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
        assertEquals(NotchRuntime.UNDEFINED, result);
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
    public void testExceptionsPropogateProperly() {
        assertThrows(CustomException.class, () -> evalNoCatch("foo.throwsException(x)", "foo", new SampleClass(), "x", 10));
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
    public static class CustomException extends RuntimeException {}
}
