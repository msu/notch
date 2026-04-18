package bigsky.notch.runtime;

import bigsky.notch.expressions.NotchExpression;
import bigsky.notch.statements.NotchStatement;
import bigsky.notch.chisel.Token;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.*;

import static bigsky.notch.runtime.NotchRuntime.UNDEFINED;

public record NotchClosure(NotchRuntime closure, List<Token> parameters, NotchExpression expression,
                           List<NotchStatement> statements)
        implements Function, Runnable, Callable, Consumer, Supplier, Predicate, BiConsumer {

    public Object call(List<Object> args) {
        if (args.size() != parameters.size()) {
            throw new IllegalArgumentException(String.format("The number of arguments, %s, does not match the number of parameters of this closure, %s", args.size(), parameters.size()));
        }
        try (var scopeLock = closure.pushScope(expression.fileId, expression.span())) {
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                String param = parameters.get(i).str();
                scopeLock.define(param, arg);
            }
            if (expression != null) {
                return closure.evaluate(expression);
            } else {
                for (NotchStatement statement : statements) {
                    closure.execute(statement);
                }
                // TODO - support return statements eventually
                return UNDEFINED;
            }
        }
    }

    public String getQualifiedName() {
        var ste = closure.stackTraceElements.getLast();

        return "<closure:%s:%d:%d>".formatted(ste.file(), ste.span().start().line, ste.span().start().column);
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