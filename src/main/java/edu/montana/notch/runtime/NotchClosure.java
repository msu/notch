package edu.montana.notch.runtime;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.statements.NotchStatement;
import edu.montana.notch.types.NotchFunction;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.util.BetterList;

import java.util.List;

public record NotchClosure(Span span, NotchRuntime closure, List<Token> parameters, NotchExpression expression,
                           List<NotchStatement> statements, String name) implements NotchFunction {

    @Override
    public String getName() {
        if (name != null) {
            return "<function:%s>".formatted(name);
        }
        // TODO: dillon
//        var ste = closure.stackTrace.getLast();
//        final var span = ste.span();
//        return "<closure:%s:%d:%d>".formatted(span.sourceId(), span.start().line, span.start().column);
        throw new UnsupportedOperationException();
    }

    public Object call(Object... args) {
        var argv = new BetterList<>(args);
        return invoke(argv);
    }

    public Object invoke(List<Object> args) {
        if (args.size() != parameters.size()) {
            throw new IllegalArgumentException(String.format("The number of arguments, %s, does not match the number of parameters of this closure, %s", args.size(), parameters.size()));
        }
        try (var localScope = closure.pushScope()) {
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                String param = parameters.get(i).str();
                localScope.define(param, arg);
            }
            if (expression != null) {
                return closure.evaluate(expression);
            } else {
                try {
                    for (NotchStatement statement : statements) {
                        closure.execute(statement);
                    }
                    return NotchRuntime.UNDEFINED;
                } catch (NotchControlFlow.Return r) {
                    return r.value;
                } catch (NotchControlFlow.Continue | NotchControlFlow.Break ignored) {
                    throw new IllegalStateException("method handler caught break/continue!");
                }
            }
        }
    }

    @Override
    public Object invoke(NotchRuntime runtime, Span callsite, List<Object> args, List<Span> argSpan) {
        try (var _guard = runtime.hideStackFrames()) {
            return invoke(args);
        }
    }

    @Override
    public boolean canInvokeWith(NotchRuntime runtime, List<NotchType> args) {
        return args.size() == parameters.size();
    }
}