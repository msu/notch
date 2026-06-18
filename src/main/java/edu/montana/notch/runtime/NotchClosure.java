package edu.montana.notch.runtime;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.statements.NotchReturn;
import edu.montana.notch.statements.NotchStatement;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.*;

import static edu.montana.notch.runtime.NotchRuntime.UNDEFINED;

public record NotchClosure(NotchRuntime closure, List<Token> parameters, NotchExpression expression,
                           List<NotchStatement> statements, String name)
        implements Function, Runnable, Callable, Consumer, Supplier, Predicate, BiConsumer {

    public Object call(List<Object> args) {
        if (args.size() != parameters.size()) {
            throw new IllegalArgumentException(String.format("The number of arguments, %s, does not match the number of parameters of this closure, %s", args.size(), parameters.size()));
        }
        try (var scopeLock = closure.pushScope()) {
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                String param = parameters.get(i).str();
                scopeLock.define(param, arg);
            }
            if (expression != null) {
                return closure.evaluate(expression);
            } else {
                try {
                    for (NotchStatement statement : statements) {
                        closure.execute(statement);
                    }
                    return UNDEFINED;
                } catch (NotchRuntimeException nre) {
                    if (nre.getCause() instanceof NotchReturn.ReturnSignal rs) {
                        return rs.value;
                    }
                    throw nre;
                }
            }
        }
    }

    public String getQualifiedName() {
        if (name != null) {
            return "<function:%s>".formatted(name);
        }
        var ste = closure.stackTraceElements.getLast();
        final var span = ste.span();
        return "<closure:%s:%d:%d>".formatted(span.sourceId(), span.start().line, span.start().column);
    }

    // implement a bunch of functional interfaces in java to make NotchClosures work w/various APIs

    @Override
    public void run() {
        this.call(Collections.emptyList());
    }

    @Override
    public Object call() {
        return this.call(Collections.emptyList());
    }

    @Override
    public Object apply(Object one) {
        return this.call(Collections.singletonList(one));
    }

    @Override
    public void accept(Object one) {
        call(Collections.singletonList(one));
    }

    @Override
    public boolean test(Object one) {
        return closure.isTruthy(this.call(Collections.singletonList(one)));
    }

    @Override
    public Object get() {
        return call(Collections.emptyList());
    }

    @Override
    public void accept(Object one, Object two) {
        call(List.of(one, two));
    }

    public BiFunction toBiFunction() {
        return (o, o2) -> call(List.of(o, o2));
    }

    public BiPredicate toBiPredicate() {
        return (o, o2) -> closure.isTruthy(call(List.of(o, o2)));
    }
}