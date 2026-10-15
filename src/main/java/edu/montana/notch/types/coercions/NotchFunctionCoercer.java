package edu.montana.notch.types.coercions;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchBoundMethod;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.NotchBoundJavaFunction;
import edu.montana.notch.types.NotchFunction;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.NotchTypeCoercer;
import edu.montana.notch.types.function.*;

import java.util.concurrent.Callable;
import java.util.function.*;

public class NotchFunctionCoercer implements NotchTypeCoercer {
    @Override
    public int getCoercionCost(NotchType source, NotchType target) {
        var sourceClass = source.getBackingClass();
        var targetClass = target.getBackingClass();

        if (sourceClass == null || targetClass == null) return NO_COERCE;

        if (NotchFunction.class.isAssignableFrom(sourceClass) || NotchFunctionProxy.class.isAssignableFrom(sourceClass)) {
            if (Runnable.class == targetClass) return CHEAP;
            if (Callable.class == targetClass) return CHEAP;
            if (BiConsumer.class == targetClass) return CHEAP;
            if (BiFunction.class == targetClass) return CHEAP;
            if (BinaryOperator.class == targetClass) return CHEAP;
            if (BiPredicate.class == targetClass) return CHEAP;
            if (BooleanSupplier.class == targetClass) return CHEAP;
            if (Consumer.class == targetClass) return CHEAP;
            if (DoubleBinaryOperator.class == targetClass) return CHEAP;
            if (DoubleConsumer.class == targetClass) return CHEAP;
            if (DoubleFunction.class == targetClass) return CHEAP;
            if (DoublePredicate.class == targetClass) return CHEAP;
            if (DoubleSupplier.class == targetClass) return CHEAP;
            if (DoubleToIntFunction.class == targetClass) return CHEAP;
            if (DoubleToLongFunction.class == targetClass) return CHEAP;
            if (DoubleUnaryOperator.class == targetClass) return CHEAP;
            if (Function.class == targetClass) return CHEAP;
            if (IntBinaryOperator.class == targetClass) return CHEAP;
            if (IntConsumer.class == targetClass) return CHEAP;
            if (IntFunction.class == targetClass) return CHEAP;
            if (IntPredicate.class == targetClass) return CHEAP;
            if (IntSupplier.class == targetClass) return CHEAP;
            if (IntToDoubleFunction.class == targetClass) return CHEAP;
            if (IntToLongFunction.class == targetClass) return CHEAP;
            if (IntUnaryOperator.class == targetClass) return CHEAP;
            if (LongBinaryOperator.class == targetClass) return CHEAP;
            if (LongConsumer.class == targetClass) return CHEAP;
            if (LongFunction.class == targetClass) return CHEAP;
            if (LongPredicate.class == targetClass) return CHEAP;
            if (LongToDoubleFunction.class == targetClass) return CHEAP;
            if (LongToIntFunction.class == targetClass) return CHEAP;
            if (LongUnaryOperator.class == targetClass) return CHEAP;
            if (ObjDoubleConsumer.class == targetClass) return CHEAP;
            if (ObjIntConsumer.class == targetClass) return CHEAP;
            if (ObjLongConsumer.class == targetClass) return CHEAP;
            if (Predicate.class == targetClass) return CHEAP;
            if (Supplier.class == targetClass) return CHEAP;
            if (ToDoubleBiFunction.class == targetClass) return CHEAP;
            if (ToDoubleFunction.class == targetClass) return CHEAP;
            if (ToIntBiFunction.class == targetClass) return CHEAP;
            if (ToIntFunction.class == targetClass) return CHEAP;
            if (ToLongBiFunction.class == targetClass) return CHEAP;
            if (ToLongFunction.class == targetClass) return CHEAP;
            if (UnaryOperator.class == targetClass) return CHEAP;
        }


        if (NotchFunction.class == targetClass) {
            if (NotchFunctionProxy.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (NotchType.class.isAssignableFrom(sourceClass)) return EXPENSIVE;
            if (Runnable.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (Callable.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (BiConsumer.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (BiFunction.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (BinaryOperator.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (BiPredicate.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (BooleanSupplier.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (Consumer.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (DoubleBinaryOperator.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (DoubleConsumer.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (DoubleFunction.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (DoublePredicate.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (DoubleSupplier.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (DoubleToIntFunction.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (DoubleToLongFunction.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (DoubleUnaryOperator.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (Function.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (IntBinaryOperator.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (IntConsumer.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (IntFunction.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (IntPredicate.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (IntSupplier.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (IntToDoubleFunction.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (IntToLongFunction.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (IntUnaryOperator.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (LongBinaryOperator.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (LongConsumer.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (LongFunction.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (LongPredicate.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (LongToDoubleFunction.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (LongToIntFunction.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (LongUnaryOperator.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (ObjDoubleConsumer.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (ObjIntConsumer.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (ObjLongConsumer.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (Predicate.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (Supplier.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (ToDoubleBiFunction.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (ToDoubleFunction.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (ToIntBiFunction.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (ToIntFunction.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (ToLongBiFunction.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (ToLongFunction.class.isAssignableFrom(sourceClass)) return CHEAP;
            if (UnaryOperator.class.isAssignableFrom(sourceClass)) return CHEAP;
            return NO_COERCE;
        }

        return NO_COERCE;
    }

    @Override
    public Object coerce(NotchRuntime runtime, Object value, Span callsite, NotchType source, NotchType target) {
        var sourceClass = source.getBackingClass();
        var targetClass = target.getBackingClass();
        if (sourceClass == null || targetClass == null) throw new CoercionFailure();

        if (targetClass == NotchFunction.class) {
            if (value instanceof NotchFunctionProxy proxy) {
                return proxy.getNotchFunction();
            }

            if (value instanceof NotchType type) {
                return new ConstructorNotchFunction(type);
            }

            if (value instanceof Runnable f) return new NotchBoundMethod(f, source.getMethod("run"));
            if (value instanceof Callable<?> f) return new NotchBoundMethod(f, source.getMethod("call"));
            if (value instanceof BiConsumer<?, ?> f) return new NotchBoundMethod(f, source.getMethod("accept"));
            if (value instanceof BinaryOperator<?> f) return new NotchBoundMethod(f, source.getMethod("apply"));
            if (value instanceof BiFunction<?, ?, ?> f) return new NotchBoundMethod(f, source.getMethod("apply"));
            if (value instanceof BiPredicate<?, ?> f) return new NotchBoundMethod(f, source.getMethod("test"));
            if (value instanceof BooleanSupplier f) return new NotchBoundMethod(f, source.getMethod("getAsBoolean"));
            if (value instanceof Consumer<?> f) return new NotchBoundMethod(f, source.getMethod("accept"));
            if (value instanceof DoubleBinaryOperator f) return new NotchBoundMethod(f, source.getMethod("applyAsDouble"));
            if (value instanceof DoubleConsumer f) return new NotchBoundMethod(f, source.getMethod("accept"));
            if (value instanceof DoubleFunction<?> f) return new NotchBoundMethod(f, source.getMethod("apply"));
            if (value instanceof DoublePredicate f) return new NotchBoundMethod(f, source.getMethod("test"));
            if (value instanceof DoubleSupplier f) return new NotchBoundMethod(f, source.getMethod("getAsDouble"));
            if (value instanceof DoubleToIntFunction f) return new NotchBoundMethod(f, source.getMethod("applyAsInt"));
            if (value instanceof DoubleToLongFunction f) return new NotchBoundMethod(f, source.getMethod("applyAsLong"));
            if (value instanceof DoubleUnaryOperator f) return new NotchBoundMethod(f, source.getMethod("applyAsDouble"));
            if (value instanceof UnaryOperator<?> f) return new NotchBoundMethod(f, source.getMethod("identity"));
            if (value instanceof Function<?, ?> f) return new NotchBoundMethod(f, source.getMethod("apply"));
            if (value instanceof IntBinaryOperator f) return new NotchBoundMethod(f, source.getMethod("applyAsInt"));
            if (value instanceof IntConsumer f) return new NotchBoundMethod(f, source.getMethod("accept"));
            if (value instanceof IntFunction<?> f) return new NotchBoundMethod(f, source.getMethod("apply"));
            if (value instanceof IntPredicate f) return new NotchBoundMethod(f, source.getMethod("test"));
            if (value instanceof IntSupplier f) return new NotchBoundMethod(f, source.getMethod("getAsInt"));
            if (value instanceof IntToDoubleFunction f) return new NotchBoundMethod(f, source.getMethod("applyAsDouble"));
            if (value instanceof IntToLongFunction f) return new NotchBoundMethod(f, source.getMethod("applyAsLong"));
            if (value instanceof IntUnaryOperator f) return new NotchBoundMethod(f, source.getMethod("applyAsInt"));
            if (value instanceof LongBinaryOperator f) return new NotchBoundMethod(f, source.getMethod("applyAsLong"));
            if (value instanceof LongConsumer f) return new NotchBoundMethod(f, source.getMethod("accept"));
            if (value instanceof LongFunction<?> f) return new NotchBoundMethod(f, source.getMethod("apply"));
            if (value instanceof LongPredicate f) return new NotchBoundMethod(f, source.getMethod("test"));
            if (value instanceof LongToDoubleFunction f) return new NotchBoundMethod(f, source.getMethod("applyAsDouble"));
            if (value instanceof LongToIntFunction f) return new NotchBoundMethod(f, source.getMethod("applyAsInt"));
            if (value instanceof LongUnaryOperator f) return new NotchBoundMethod(f, source.getMethod("applyAsLong"));
            if (value instanceof ObjDoubleConsumer<?> f) return new NotchBoundMethod(f, source.getMethod("accept"));
            if (value instanceof ObjIntConsumer<?> f) return new NotchBoundMethod(f, source.getMethod("accept"));
            if (value instanceof ObjLongConsumer<?> f) return new NotchBoundMethod(f, source.getMethod("accept"));
            if (value instanceof Predicate<?> f) return new NotchBoundMethod(f, source.getMethod("test"));
            if (value instanceof Supplier<?> f) return new NotchBoundMethod(f, source.getMethod("get"));
            if (value instanceof ToDoubleBiFunction<?, ?> f) return new NotchBoundMethod(f, source.getMethod("applyAsDouble"));
            if (value instanceof ToDoubleFunction<?> f) return new NotchBoundMethod(f, source.getMethod("applyAsDouble"));
            if (value instanceof ToIntBiFunction<?, ?> f) return new NotchBoundMethod(f, source.getMethod("applyAsInt"));
            if (value instanceof ToIntFunction<?> f) return new NotchBoundMethod(f, source.getMethod("applyAsInt"));
            if (value instanceof ToLongBiFunction<?, ?> f) return new NotchBoundMethod(f, source.getMethod("applyAsLong"));
            if (value instanceof ToLongFunction<?> f) return new NotchBoundMethod(f, source.getMethod("applyAsLong"));

            throw new CoercionFailure();
        }

        NotchFunction function;
        if (value instanceof NotchFunction it) {
            function = it;
        } else if (value instanceof NotchFunctionProxy it) {
            function = it.getNotchFunction();
        } else {
            throw new CoercionFailure();
        }

        if (targetClass == Runnable.class) return new RunnableNotchFunction(runtime, callsite, function);
        if (targetClass == Callable.class) return new CallableNotchFunction(runtime, callsite, function);
        if (targetClass == BiConsumer.class) return new BiConsumerNotchFunction(runtime, callsite, function);
        if (targetClass == BiFunction.class) return new BiFunctionNotchFunction(runtime, callsite, function);
        if (targetClass == BinaryOperator.class) return new BinaryOperatorNotchFunction(runtime, callsite, function);
        if (targetClass == BiPredicate.class) return new BiPredicateNotchFunction(runtime, callsite, function);
        if (targetClass == BooleanSupplier.class) return new BooleanSupplierNotchFunction(runtime, callsite, function);
        if (targetClass == Consumer.class) return new ConsumerNotchFunction(runtime, callsite, function);
        if (targetClass == DoubleBinaryOperator.class) return new DoubleBinaryOperatorNotchFunction(runtime, callsite, function);
        if (targetClass == DoubleConsumer.class) return new DoubleConsumerNotchFunction(runtime, callsite, function);
        if (targetClass == DoubleFunction.class) return new DoubleFunctionNotchFunction(runtime, callsite, function);
        if (targetClass == DoublePredicate.class) return new DoublePredicateNotchFunction(runtime, callsite, function);
        if (targetClass == DoubleSupplier.class) return new DoubleSupplierNotchFunction(runtime, callsite, function);
        if (targetClass == DoubleToIntFunction.class) return new DoubleToIntFunctionNotchFunction(runtime, callsite, function);
        if (targetClass == DoubleToLongFunction.class) return new DoubleToLongFunctionNotchFunction(runtime, callsite, function);
        if (targetClass == DoubleUnaryOperator.class) return new DoubleUnaryOperatorNotchFunction(runtime, callsite, function);
        if (targetClass == Function.class) return new FunctionNotchFunction(runtime, callsite, function);
        if (targetClass == IntBinaryOperator.class) return new IntBinaryOperatorNotchFunction(runtime, callsite, function);
        if (targetClass == IntConsumer.class) return new IntConsumerNotchFunction(runtime, callsite, function);
        if (targetClass == IntFunction.class) return new IntFunctionNotchFunction(runtime, callsite, function);
        if (targetClass == IntPredicate.class) return new IntPredicateNotchFunction(runtime, callsite, function);
        if (targetClass == IntSupplier.class) return new IntSupplierNotchFunction(runtime, callsite, function);
        if (targetClass == IntToDoubleFunction.class) return new IntToDoubleFunctionNotchFunction(runtime, callsite, function);
        if (targetClass == IntToLongFunction.class) return new IntToLongFunctionNotchFunction(runtime, callsite, function);
        if (targetClass == IntUnaryOperator.class) return new IntUnaryOperatorNotchFunction(runtime, callsite, function);
        if (targetClass == LongBinaryOperator.class) return new LongBinaryOperatorNotchFunction(runtime, callsite, function);
        if (targetClass == LongConsumer.class) return new LongConsumerNotchFunction(runtime, callsite, function);
        if (targetClass == LongFunction.class) return new LongFunctionNotchFunction(runtime, callsite, function);
        if (targetClass == LongPredicate.class) return new LongPredicateNotchFunction(runtime, callsite, function);
        if (targetClass == LongToDoubleFunction.class) return new LongToDoubleFunctionNotchFunction(runtime, callsite, function);
        if (targetClass == LongToIntFunction.class) return new LongToIntFunctionNotchFunction(runtime, callsite, function);
        if (targetClass == LongUnaryOperator.class) return new LongUnaryOperatorNotchFunction(runtime, callsite, function);
        if (targetClass == ObjDoubleConsumer.class) return new ObjDoubleConsumerNotchFunction(runtime, callsite, function);
        if (targetClass == ObjIntConsumer.class) return new ObjIntConsumerNotchFunction(runtime, callsite, function);
        if (targetClass == ObjLongConsumer.class) return new ObjLongConsumerNotchFunction(runtime, callsite, function);
        if (targetClass == Predicate.class) return new PredicateNotchFunction(runtime, callsite, function);
        if (targetClass == Supplier.class) return new SupplierNotchFunction(runtime, callsite, function);
        if (targetClass == ToDoubleBiFunction.class) return new ToDoubleBiFunctionNotchFunction(runtime, callsite, function);
        if (targetClass == ToDoubleFunction.class) return new ToDoubleFunctionNotchFunction(runtime, callsite, function);
        if (targetClass == ToIntBiFunction.class) return new ToIntBiFunctionNotchFunction(runtime, callsite, function);
        if (targetClass == ToIntFunction.class) return new ToIntFunctionNotchFunction(runtime, callsite, function);
        if (targetClass == ToLongBiFunction.class) return new ToLongBiFunctionNotchFunction(runtime, callsite, function);
        if (targetClass == ToLongFunction.class) return new ToLongFunctionNotchFunction(runtime, callsite, function);
        if (targetClass == UnaryOperator.class) return new UnaryOperatorNotchFunction(runtime, callsite, function);
        throw new CoercionFailure();
    }
}
