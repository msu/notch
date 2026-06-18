package edu.montana.notch.statements;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchInstance;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;

public class NotchPropertyAssignment extends NotchStatement {
    private final NotchExpression root;
    private final Token property;
    private final NotchExpression value;

    public NotchPropertyAssignment(Span span, NotchExpression root, Token property, NotchExpression value) {
        super(span);
        this.root = addChild(root);
        this.property = property;
        this.value = addChild(value);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        Object rootValue = runtime.evaluate(root);
        if (!(rootValue instanceof NotchInstance instance)) {
            var diag = new Diagnostic();
            diag.setTitle("cannot assign a property on a non-instance value");
            diag.highlight(span());
            diag.note("the target was " + NotchRuntime.className(rootValue));
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }
        if (!instance.type.declaresField(property.str())) {
            var diag = new Diagnostic();
            diag.setTitle("no such field");
            diag.highlight(span());
            String note = instance.type.name + " has no field '" + property.str() + "'";
            String suggestion = instance.type.closestDeclaredField(property.str());
            if (suggestion != null) {
                note += " - Did you mean '" + suggestion + "'?";
            }
            diag.note(note);
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }
        Object v = runtime.evaluate(value);
        instance.fields.put(property.str(), v);
    }
}
