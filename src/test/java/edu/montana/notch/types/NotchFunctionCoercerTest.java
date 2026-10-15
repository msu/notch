package edu.montana.notch.types;

import edu.montana.notch.types.function.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.*;

import static edu.montana.notch.NotchTestUtils.eval;
import static org.junit.jupiter.api.Assertions.*;

class NotchFunctionCoercerTest {

    private static <T> T coerce(Class<T> clazz, String closure, Object... vars) {
        T out = eval(clazz, closure, vars);
        assertInstanceOf(clazz, out);
        assertInstanceOf(NotchFunctionProxy.class, out);
        return out;
    }

    @Test
    void runnable() {
        List<Object> out = new ArrayList<>();
        Runnable fn = coerce(Runnable.class, "\\ -> out.add(1)", "out", out);
        assertInstanceOf(RunnableNotchFunction.class, fn);
        fn.run();
        assertEquals(List.of(1), out);
    }

    @Test
    void callable() throws Exception {
        Callable<?> fn = coerce(Callable.class, "\\ -> 42");
        assertInstanceOf(CallableNotchFunction.class, fn);
        assertEquals(42, fn.call());
    }

    @Test
    @SuppressWarnings("unchecked")
    void biConsumer() {
        List<Object> out = new ArrayList<>();
        BiConsumer<Object, Object> fn = coerce(BiConsumer.class, "\\ a, b -> out.add(a + b)", "out", out);
        assertInstanceOf(BiConsumerNotchFunction.class, fn);
        fn.accept(2, 3);
        assertEquals(List.of(5), out);
    }

    @Test
    @SuppressWarnings("unchecked")
    void biFunction() {
        BiFunction<Object, Object, Object> fn = coerce(BiFunction.class, "\\ a, b -> a - b");
        assertInstanceOf(BiFunctionNotchFunction.class, fn);
        assertEquals(2, fn.apply(5, 3));
    }

    @Test
    @SuppressWarnings("unchecked")
    void binaryOperator() {
        BinaryOperator<Object> fn = coerce(BinaryOperator.class, "\\ a, b -> a - b");
        assertInstanceOf(BinaryOperatorNotchFunction.class, fn);
        assertEquals(2, fn.apply(5, 3));
    }

    @Test
    @SuppressWarnings("unchecked")
    void biPredicate() {
        BiPredicate<Object, Object> fn = coerce(BiPredicate.class, "\\ a, b -> a > b");
        assertInstanceOf(BiPredicateNotchFunction.class, fn);
        assertTrue(fn.test(5, 3));
        assertFalse(fn.test(3, 5));
    }

    @Test
    void booleanSupplier() {
        BooleanSupplier fn = coerce(BooleanSupplier.class, "\\ -> true");
        assertInstanceOf(BooleanSupplierNotchFunction.class, fn);
        assertTrue(fn.getAsBoolean());
    }

    @Test
    @SuppressWarnings("unchecked")
    void consumer() {
        List<Object> out = new ArrayList<>();
        Consumer<Object> fn = coerce(Consumer.class, "\\ x -> out.add(x)", "out", out);
        assertInstanceOf(ConsumerNotchFunction.class, fn);
        fn.accept("hi");
        assertEquals(List.of("hi"), out);
    }

    @Test
    void doubleBinaryOperator() {
        DoubleBinaryOperator fn = coerce(DoubleBinaryOperator.class, "\\ a, b -> a * b");
        assertInstanceOf(DoubleBinaryOperatorNotchFunction.class, fn);
        assertEquals(7.5, fn.applyAsDouble(2.5, 3.0));
    }

    @Test
    void doubleConsumer() {
        List<Object> out = new ArrayList<>();
        DoubleConsumer fn = coerce(DoubleConsumer.class, "\\ x -> out.add(x)", "out", out);
        assertInstanceOf(DoubleConsumerNotchFunction.class, fn);
        fn.accept(1.5);
        assertEquals(List.of(1.5), out);
    }

    @Test
    void doubleFunction() {
        DoubleFunction<?> fn = coerce(DoubleFunction.class, "\\ x -> x * 2");
        assertInstanceOf(DoubleFunctionNotchFunction.class, fn);
        assertEquals(3.0, fn.apply(1.5));
    }

    @Test
    void doublePredicate() {
        DoublePredicate fn = coerce(DoublePredicate.class, "\\ x -> x > one", "one", 1.0);
        assertInstanceOf(DoublePredicateNotchFunction.class, fn);
        assertTrue(fn.test(1.5));
        assertFalse(fn.test(0.5));
    }

    @Test
    void doubleSupplier() {
        DoubleSupplier fn = coerce(DoubleSupplier.class, "\\ -> v", "v", 2.5);
        assertInstanceOf(DoubleSupplierNotchFunction.class, fn);
        assertEquals(2.5, fn.getAsDouble());
    }

    @Test
    void doubleToIntFunction() {
        DoubleToIntFunction fn = coerce(DoubleToIntFunction.class, "\\ x -> 7");
        assertInstanceOf(DoubleToIntFunctionNotchFunction.class, fn);
        assertEquals(7, fn.applyAsInt(1.5));
    }

    @Test
    void doubleToLongFunction() {
        DoubleToLongFunction fn = coerce(DoubleToLongFunction.class, "\\ x -> 7");
        assertInstanceOf(DoubleToLongFunctionNotchFunction.class, fn);
        assertEquals(7L, fn.applyAsLong(1.5));
    }

    @Test
    void doubleUnaryOperator() {
        DoubleUnaryOperator fn = coerce(DoubleUnaryOperator.class, "\\ x -> x + one", "one", 1.0);
        assertInstanceOf(DoubleUnaryOperatorNotchFunction.class, fn);
        assertEquals(2.5, fn.applyAsDouble(1.5));
    }

    @Test
    @SuppressWarnings("unchecked")
    void function() {
        Function<Object, Object> fn = coerce(Function.class, "\\ x -> x + 1");
        assertInstanceOf(FunctionNotchFunction.class, fn);
        assertEquals(6, fn.apply(5));
    }

    @Test
    void intBinaryOperator() {
        IntBinaryOperator fn = coerce(IntBinaryOperator.class, "\\ a, b -> a + b");
        assertInstanceOf(IntBinaryOperatorNotchFunction.class, fn);
        assertEquals(5, fn.applyAsInt(2, 3));
    }

    @Test
    void intConsumer() {
        List<Object> out = new ArrayList<>();
        IntConsumer fn = coerce(IntConsumer.class, "\\ x -> out.add(x)", "out", out);
        assertInstanceOf(IntConsumerNotchFunction.class, fn);
        fn.accept(9);
        assertEquals(List.of(9), out);
    }

    @Test
    void intFunction() {
        IntFunction<?> fn = coerce(IntFunction.class, "\\ x -> x * 3");
        assertInstanceOf(IntFunctionNotchFunction.class, fn);
        assertEquals(12, fn.apply(4));
    }

    @Test
    void intPredicate() {
        IntPredicate fn = coerce(IntPredicate.class, "\\ x -> x > 3");
        assertInstanceOf(IntPredicateNotchFunction.class, fn);
        assertTrue(fn.test(4));
        assertFalse(fn.test(2));
    }

    @Test
    void intSupplier() {
        IntSupplier fn = coerce(IntSupplier.class, "\\ -> 11");
        assertInstanceOf(IntSupplierNotchFunction.class, fn);
        assertEquals(11, fn.getAsInt());
    }

    @Test
    void intToDoubleFunction() {
        IntToDoubleFunction fn = coerce(IntToDoubleFunction.class, "\\ x -> x * half", "half", 0.5);
        assertInstanceOf(IntToDoubleFunctionNotchFunction.class, fn);
        assertEquals(2.0, fn.applyAsDouble(4));
    }

    @Test
    void intToLongFunction() {
        IntToLongFunction fn = coerce(IntToLongFunction.class, "\\ x -> x + 1");
        assertInstanceOf(IntToLongFunctionNotchFunction.class, fn);
        assertEquals(5L, fn.applyAsLong(4));
    }

    @Test
    void intUnaryOperator() {
        IntUnaryOperator fn = coerce(IntUnaryOperator.class, "\\ x -> x - 1");
        assertInstanceOf(IntUnaryOperatorNotchFunction.class, fn);
        assertEquals(3, fn.applyAsInt(4));
    }

    @Test
    void longBinaryOperator() {
        LongBinaryOperator fn = coerce(LongBinaryOperator.class, "\\ a, b -> a + b");
        assertInstanceOf(LongBinaryOperatorNotchFunction.class, fn);
        assertEquals(5L, fn.applyAsLong(2L, 3L));
    }

    @Test
    void longConsumer() {
        List<Object> out = new ArrayList<>();
        LongConsumer fn = coerce(LongConsumer.class, "\\ x -> out.add(x)", "out", out);
        assertInstanceOf(LongConsumerNotchFunction.class, fn);
        fn.accept(9L);
        assertEquals(List.of(9L), out);
    }

    @Test
    void longFunction() {
        LongFunction<?> fn = coerce(LongFunction.class, "\\ x -> x");
        assertInstanceOf(LongFunctionNotchFunction.class, fn);
        assertEquals(4L, fn.apply(4L));
    }

    @Test
    void longPredicate() {
        LongPredicate fn = coerce(LongPredicate.class, "\\ x -> x > 3");
        assertInstanceOf(LongPredicateNotchFunction.class, fn);
        assertTrue(fn.test(4L));
        assertFalse(fn.test(2L));
    }

    @Test
    void longToDoubleFunction() {
        LongToDoubleFunction fn = coerce(LongToDoubleFunction.class, "\\ x -> v", "v", 2.5);
        assertInstanceOf(LongToDoubleFunctionNotchFunction.class, fn);
        assertEquals(2.5, fn.applyAsDouble(4L));
    }

    @Test
    void longToIntFunction() {
        LongToIntFunction fn = coerce(LongToIntFunction.class, "\\ x -> 7");
        assertInstanceOf(LongToIntFunctionNotchFunction.class, fn);
        assertEquals(7, fn.applyAsInt(4L));
    }

    @Test
    void longUnaryOperator() {
        LongUnaryOperator fn = coerce(LongUnaryOperator.class, "\\ x -> x + 1");
        assertInstanceOf(LongUnaryOperatorNotchFunction.class, fn);
        assertEquals(5L, fn.applyAsLong(4L));
    }

    @Test
    @SuppressWarnings("unchecked")
    void objDoubleConsumer() {
        List<Object> out = new ArrayList<>();
        ObjDoubleConsumer<Object> fn = coerce(ObjDoubleConsumer.class, "\\ o, x -> out.add(x)", "out", out);
        assertInstanceOf(ObjDoubleConsumerNotchFunction.class, fn);
        fn.accept("a", 1.5);
        assertEquals(List.of(1.5), out);
    }

    @Test
    @SuppressWarnings("unchecked")
    void objIntConsumer() {
        List<Object> out = new ArrayList<>();
        ObjIntConsumer<Object> fn = coerce(ObjIntConsumer.class, "\\ o, x -> out.add(o)", "out", out);
        assertInstanceOf(ObjIntConsumerNotchFunction.class, fn);
        fn.accept("a", 1);
        assertEquals(List.of("a"), out);
    }

    @Test
    @SuppressWarnings("unchecked")
    void objLongConsumer() {
        List<Object> out = new ArrayList<>();
        ObjLongConsumer<Object> fn = coerce(ObjLongConsumer.class, "\\ o, x -> out.add(x)", "out", out);
        assertInstanceOf(ObjLongConsumerNotchFunction.class, fn);
        fn.accept("a", 3L);
        assertEquals(List.of(3L), out);
    }

    @Test
    @SuppressWarnings("unchecked")
    void predicate() {
        Predicate<Object> fn = coerce(Predicate.class, "\\ x -> (x is 'yes')");
        assertInstanceOf(PredicateNotchFunction.class, fn);
        assertTrue(fn.test("yes"));
        assertFalse(fn.test("no"));
    }

    @Test
    void supplier() {
        Supplier<?> fn = coerce(Supplier.class, "\\ -> 'hello'");
        assertInstanceOf(SupplierNotchFunction.class, fn);
        assertEquals("hello", fn.get());
    }

    @Test
    @SuppressWarnings("unchecked")
    void toDoubleBiFunction() {
        ToDoubleBiFunction<Object, Object> fn = coerce(ToDoubleBiFunction.class, "\\ a, b -> a * b");
        assertInstanceOf(ToDoubleBiFunctionNotchFunction.class, fn);
        assertEquals(3.0, fn.applyAsDouble(1.5, 2.0));
    }

    @Test
    @SuppressWarnings("unchecked")
    void toDoubleFunction() {
        ToDoubleFunction<Object> fn = coerce(ToDoubleFunction.class, "\\ x -> v", "v", 2.5);
        assertInstanceOf(ToDoubleFunctionNotchFunction.class, fn);
        assertEquals(2.5, fn.applyAsDouble("a"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void toIntBiFunction() {
        ToIntBiFunction<Object, Object> fn = coerce(ToIntBiFunction.class, "\\ a, b -> a + b");
        assertInstanceOf(ToIntBiFunctionNotchFunction.class, fn);
        assertEquals(5, fn.applyAsInt(2, 3));
    }

    @Test
    @SuppressWarnings("unchecked")
    void toIntFunction() {
        ToIntFunction<Object> fn = coerce(ToIntFunction.class, "\\ x -> x + 1");
        assertInstanceOf(ToIntFunctionNotchFunction.class, fn);
        assertEquals(3, fn.applyAsInt(2));
    }

    @Test
    @SuppressWarnings("unchecked")
    void toLongBiFunction() {
        ToLongBiFunction<Object, Object> fn = coerce(ToLongBiFunction.class, "\\ a, b -> a + b");
        assertInstanceOf(ToLongBiFunctionNotchFunction.class, fn);
        assertEquals(5L, fn.applyAsLong(2, 3));
    }

    @Test
    @SuppressWarnings("unchecked")
    void toLongFunction() {
        ToLongFunction<Object> fn = coerce(ToLongFunction.class, "\\ x -> x + 1");
        assertInstanceOf(ToLongFunctionNotchFunction.class, fn);
        assertEquals(3L, fn.applyAsLong(2));
    }

    @Test
    @SuppressWarnings("unchecked")
    void unaryOperator() {
        UnaryOperator<Object> fn = coerce(UnaryOperator.class, "\\ x -> x * 2");
        assertInstanceOf(UnaryOperatorNotchFunction.class, fn);
        assertEquals(8, fn.apply(4));
    }

    @Test
    void proxyCoercesBackToOriginalNotchFunction() {
        Function<?, ?> fn = coerce(Function.class, "\\ x -> x");
        NotchFunction original = ((NotchFunctionProxy) fn).getNotchFunction();
        assertSame(original, eval(NotchFunction.class, "f", "f", fn));
    }

    @Test
    @SuppressWarnings("unchecked")
    void functionProxyCoercesToPredicate() {
        Function<?, ?> fn = coerce(Function.class, "\\ x -> (x is 'yes')");
        Predicate<Object> predicate = coerce(Predicate.class, "f", "f", fn);
        assertInstanceOf(PredicateNotchFunction.class, predicate);
        assertSame(((NotchFunctionProxy) fn).getNotchFunction(), ((NotchFunctionProxy) predicate).getNotchFunction());
        assertTrue(predicate.test("yes"));
        assertFalse(predicate.test("no"));
    }

    @Test
    void supplierProxyCoercesToIntSupplier() {
        Supplier<?> fn = coerce(Supplier.class, "\\ -> 11");
        IntSupplier supplier = coerce(IntSupplier.class, "f", "f", fn);
        assertInstanceOf(IntSupplierNotchFunction.class, supplier);
        assertEquals(11, supplier.getAsInt());
    }

    @Test
    void intUnaryOperatorProxyCoercesToLongUnaryOperator() {
        IntUnaryOperator fn = coerce(IntUnaryOperator.class, "\\ x -> x + 1");
        LongUnaryOperator op = coerce(LongUnaryOperator.class, "f", "f", fn);
        assertInstanceOf(LongUnaryOperatorNotchFunction.class, op);
        assertEquals(5L, op.applyAsLong(4L));
    }

    @Test
    @SuppressWarnings("unchecked")
    void toIntBiFunctionProxyCoercesToBiFunction() {
        ToIntBiFunction<?, ?> fn = coerce(ToIntBiFunction.class, "\\ a, b -> a * b");
        BiFunction<Object, Object, Object> biFunction = coerce(BiFunction.class, "f", "f", fn);
        assertInstanceOf(BiFunctionNotchFunction.class, biFunction);
        assertEquals(12, biFunction.apply(3, 4));
    }

    @Test
    void biFunctionCoercesWhenPassedToJavaMethod() {
        Map<String, Object> map = new HashMap<>(Map.of("a", 1));
        eval("map.merge('a', 5, \\ a, b -> a + b)", "map", map);
        assertEquals(6, map.get("a"));
    }

    @Test
    void proxyAlreadyOfTargetTypeIsReturnedAsIs() {
        Function<?, ?> fn = coerce(Function.class, "\\ x -> x");
        assertSame(fn, eval(Function.class, "f", "f", fn));
    }

    @Test
    void proxyCoercesWhenPassedToJavaMethod() {
        Function<?, ?> fn = coerce(Function.class, "\\ x -> (x > 2)");
        List<Object> list = new ArrayList<>(List.of(1, 2, 3, 4));
        eval("list.removeIf(f)", "list", list, "f", fn);
        assertEquals(List.of(1, 2), list);
    }

    @Test
    void closureCannotCoerceToUnrelatedInterface() {
        assertThrows(RuntimeException.class, () -> eval(Comparable.class, "\\ x -> x"));
    }
}
