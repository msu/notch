package bigsky.notch.runtime;

import bigsky.notch.expressions.NotchExpression;
import bigsky.notch.statements.NotchStatement;
import bigsky.utils.chisel.Token;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static bigsky.notch.runtime.NotchRuntime.UNDEFINED;

public record NotchClosure(NotchRuntime closure, List<Token> parameters, NotchExpression expression,
                           List<NotchStatement> statements) implements Function, Runnable, Callable {

    public Object call(List<Object> args) {
        if(args.size() != parameters.size()) {
            throw new IllegalArgumentException("The number of parameters does not match the number of arguments");
        }
        try (var scopeLock = closure.pushScope()) {
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                String param = parameters.get(i).str();
                closure.setHard(param, arg);
            }
            if (expression != null) {
                return expression.evaluate(closure);
            } else {
                for (NotchStatement statement : statements) {
                    statement.execute(closure);
                }
                // TODO - support return statements eventually
                return UNDEFINED;
            }
        }
    }

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
}
