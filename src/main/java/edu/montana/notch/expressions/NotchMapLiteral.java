package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.util.BetterMap;
import edu.montana.notch.util.Text;

import java.util.List;

public class NotchMapLiteral extends NotchExpression {
    public final List<NotchExpression> keys;
    public final List<NotchExpression> values;

    public NotchMapLiteral(Span span, List<NotchExpression> keys, List<NotchExpression> values) {
        super(span);
        this.keys = keys;
        this.values = values;

        addChildren(keys);
        addChildren(values);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        BetterMap<Object, Object> betterMap = new BetterMap<>();
        for (int i = 0; i < keys.size(); i++) {
            NotchExpression keyExpr = keys.get(i);
            Object key = runtime.evaluate(keyExpr);
            if (runtime.isUndefined(key)) {
                throw undefinedKey(runtime, keyExpr);
            }
            betterMap.put(key, runtime.evaluate(values.get(i)));
        }
        return betterMap;
    }

    private NotchRuntimeException undefinedKey(NotchRuntime runtime, NotchExpression keyExpr) {
        var diag = new Diagnostic();
        diag.setTitle("undefined map key");
        diag.highlight(keyExpr.span());
        if (keyExpr instanceof NotchIdentifier id) {
            String name = id.name();
            diag.note("'" + name + "' is not defined, so it can't be used as a map key");
            diag.note("to use it as text, quote it: '" + name + "'");
            String suggestion = Text.closest(name, runtime.getInScopeSymbols(), 2);
            if (suggestion != null) {
                diag.note("did you mean the variable '" + suggestion + "'?");
            }
        } else {
            diag.note("this map key evaluated to <undefined>");
        }
        return new NotchRuntimeException(runtime.currentStackTrace(), diag);
    }
}
