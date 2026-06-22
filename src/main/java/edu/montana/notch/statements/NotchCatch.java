package edu.montana.notch.statements;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.templates.ast.QualifiedIdent;
import edu.montana.notch.types.TypeSystem;

import java.util.List;

public final class NotchCatch {
    public final QualifiedIdent type;
    public final String exceptionName;
    public final List<NotchStatement> body;

    public NotchCatch(QualifiedIdent type, String exceptionName, List<NotchStatement> body) {
        this.type = type;
        this.exceptionName = exceptionName;
        this.body = body;
    }

    public Class<?> resolve(NotchRuntime runtime) {
        if (type == null) return null;
        String typeName = type.qualifiedName();
        Class<?> wanted;
        if (typeName.equals("NotchError")) {
            wanted = NotchRuntimeException.class;
        } else {
            wanted = TypeSystem.resolveThrowableType(typeName);
        }
        if (wanted == null) {
            Diagnostic diag = new Diagnostic();
            diag.setTitle("unknown exception type '" + type.qualifiedName() + "'");
            diag.highlight(type);
            diag.note("expected a Throwable type (e.g. RuntimeException, IOException) or 'NotchError'");
            throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
        }
        return wanted;
    }

    public static boolean isControlFlow(Throwable t) {
        if (t instanceof NotchBreak.BreakSignal) return true;
        if (t instanceof NotchContinue.ContinueSignal) return true;
        if (t instanceof NotchReturn.ReturnSignal) return true;
        if (t == null) return false;
        Throwable cause = t.getCause();
        if (cause instanceof NotchBreak.BreakSignal) return true;
        if (cause instanceof NotchContinue.ContinueSignal) return true;
        if (cause instanceof NotchReturn.ReturnSignal) return true;
        return false;
    }

    private static Object resolveCaughtCandidate(Throwable t) {
        if (t instanceof NotchRuntimeException nre) {
            if (nre.thrownValue != null) return nre.thrownValue;
            if (nre.getCause() != null) return nre.getCause();
            return nre;
        }
        return t;
    }

    private static boolean caughtMatches(Class<?> wanted, Throwable thrown) {
        if (wanted == null) return true; //match everything
        Object candidate = resolveCaughtCandidate(thrown);
        return wanted.isInstance(candidate) || wanted.isInstance(thrown);
    }

    public static boolean catchHandled(NotchRuntime runtime, Throwable t, List<NotchCatch> clauses) {
        Object candidate = resolveCaughtCandidate(t);
        for (NotchCatch clause : clauses) {
            Class<?> wantedType = clause.resolve(runtime);
            if (caughtMatches(wantedType, t)) {
                runtime.pushHandled(t);
                try (var scope = runtime.pushScope()) {
                    scope.define("exception", candidate);
                    //allow for alternative name as well
                    if (clause.exceptionName != null) scope.define(clause.exceptionName, candidate);
                    for (NotchStatement stmt : clause.body) {
                        runtime.execute(stmt);
                    }
                } finally {
                    runtime.popHandled();
                }
                return true;
            }
        }
        return false;
    }
}
