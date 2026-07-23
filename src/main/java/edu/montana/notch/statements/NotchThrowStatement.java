package edu.montana.notch.statements;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.expressions.NotchIdentifier;
import edu.montana.notch.expressions.NotchInstantiation;
import edu.montana.notch.expressions.NotchMethodInvocation;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;

import java.util.ArrayList;
import java.util.List;

public class NotchThrowStatement extends NotchStatement {
    private final NotchExpression operand;

    public NotchThrowStatement(Span span, NotchExpression operand) {
        super(span);
        this.operand = addChild(operand);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        Object value = constructOrEvaluate(runtime);
        throw NotchRuntimeException.userThrow(runtime.currentStackTrace(), span, value);
    }

    private Object constructOrEvaluate(NotchRuntime runtime) {
        //allow for an expression to be thrown
        if (!(operand instanceof NotchMethodInvocation call)) return runtime.evaluate(operand);
        if (!(call.root instanceof NotchIdentifier id)) return runtime.evaluate(operand);
        String name = id.name();
        if (runtime.getSymbol(name) != NotchRuntime.UNDEFINED) return runtime.evaluate(operand);
        List<Object> argValues = new ArrayList<>(call.args.size());
        for (NotchExpression arg : call.args) argValues.add(runtime.evaluate(arg));
        //exception name not in scope (i.e called as throw example('msg'), attempt construction
        try {
            Object constructed = NotchInstantiation.tryConstructThrowable(name, argValues);
            if (constructed != null) return constructed;
        } catch (IllegalArgumentException e) {
            var diag = new Diagnostic();
            diag.setTitle("no constructor for '" + name + "' accepts these argument types");
            diag.highlight(span);
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }
        var diag = new Diagnostic();
        diag.setTitle("cannot throw '" + name + "': not a throwable type");
        diag.highlight(span);
        diag.note("expected a Java throwable (e.g. RuntimeException, IOException), 'NotchError', or 'new MyClass(...)' for user-defined classes");
        throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
    }
}