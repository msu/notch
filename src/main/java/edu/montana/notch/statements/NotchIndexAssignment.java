package edu.montana.notch.statements;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;

import java.util.List;
import java.util.Map;

public class NotchIndexAssignment extends NotchStatement {
    private final NotchExpression root;
    private final NotchExpression index;
    private final NotchExpression value;

    public NotchIndexAssignment(Span span, NotchExpression root, NotchExpression index, NotchExpression value) {
        super(span);
        this.root = addChild(root);
        this.index = addChild(index);
        this.value = addChild(value);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        Object rootValue = runtime.evaluate(root);
        Object indexValue = runtime.evaluate(index);
        Object newValue = runtime.evaluate(value);
        if (rootValue instanceof Map map) {
            map.put(indexValue, newValue);
        } else if (rootValue instanceof List list) {
            list.set(toInteger(indexValue), newValue);
        } else if (rootValue instanceof Object[] array) {
            array[toInteger(indexValue)] = newValue;
        } else {
            var diag = new Diagnostic();
            diag.setTitle("cannot assign into this value with an index");
            diag.highlight(span());
            diag.note("the target was " + NotchRuntime.className(rootValue));
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }
    }

    private static Integer toInteger(Object index) {
        // TODO - replace with proper coercions
        // call throws a raw ClassCastException (no Notch diagnostic) nums['one'] = 5
        return (Integer) index;
    }
}
