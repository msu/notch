package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.statements.NotchCatch;
import edu.montana.notch.templates.ast.QualifiedIdent;
import edu.montana.notch.util.Exceptions;

import java.util.List;

public class NotchRecoverExpression extends NotchExpression {

    public record TypedRecover(QualifiedIdent type, NotchExpression expr) {}

    private final NotchExpression tryExpr;
    private final List<TypedRecover> typedRecovers;
    private final NotchExpression untypedRecover;

    public NotchRecoverExpression(Span span, NotchExpression tryExpr,
                                  List<TypedRecover> typedRecovers, NotchExpression untypedRecover) {
        super(span);
        this.tryExpr = addChild(tryExpr);
        this.typedRecovers = typedRecovers;
        for (TypedRecover r : typedRecovers) addChild(r.expr());
        this.untypedRecover = untypedRecover == null ? null : addChild(untypedRecover);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        try {
            return runtime.evaluate(tryExpr);
        } catch (Throwable t) {
            if (NotchCatch.isControlFlow(t)) throw Exceptions.rethrow(t);
            return handle(runtime, t);
        }
    }

    private Object handle(NotchRuntime runtime, Throwable t) {
        Object candidate = NotchCatch.unwrap(t);
        for (TypedRecover r : typedRecovers) {
            Class<?> wanted = NotchCatch.resolveType(runtime, r.type());
            if (wanted == null || wanted.isInstance(candidate) || wanted.isInstance(t)) {
                return runtime.evaluate(r.expr());
            }
        }
        if (untypedRecover != null) return runtime.evaluate(untypedRecover);
        throw Exceptions.rethrow(t);
    }
}