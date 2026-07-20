package edu.montana.notch.statements;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.templates.ast.QualifiedIdent;
import edu.montana.notch.types.TypeSystem;
import edu.montana.notch.util.Text;

public class NotchImport extends NotchStatement {
    private final QualifiedIdent type;
    private final Token alias;

    public NotchImport(Span span, QualifiedIdent type, Token alias) {
        super(span);
        this.type = type;
        this.alias = alias;
    }

    @Override
    public void execute(NotchRuntime runtime) {
        var name = type.qualifiedName();
        var notchType = TypeSystem.getType(name);
        if (notchType == null) {
            var diag = new Diagnostic();
            diag.highlight(type.span());
            diag.note("The type is unknown for: %s".formatted(Text.repr(name)));
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }
        runtime.defineOrUpdate(alias != null ? alias.str() : notchType.getSimpleName(), notchType);
    }
}
