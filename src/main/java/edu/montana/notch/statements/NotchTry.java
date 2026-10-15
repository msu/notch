package edu.montana.notch.statements;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchControlFlow;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;

import java.util.List;

import static edu.montana.notch.util.Exceptions.rethrow;

public class NotchTry extends NotchStatement {
    private final List<NotchStatement> body;
    private final List<NotchCatch> clauses;

    public NotchTry(Span span, List<NotchStatement> body, List<NotchCatch> clauses) {
        super(span);
        this.body = body;
        this.clauses = clauses;
        addChildren(body);
        for (NotchCatch clause : clauses) addChildren(clause.body);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        try {
            for (NotchStatement stmt : body) {
                runtime.execute(stmt);
            }
        } catch (NotchControlFlow cf) {
            throw rethrow(cf);
        } catch (NotchRuntimeException nre) {
            if (!NotchCatch.catchHandled(runtime, nre, clauses)) throw nre;
        } catch (Exception e) {
            Diagnostic diag = new Diagnostic();
            diag.setTitle(e.getClass().getSimpleName() + (e.getMessage() != null ? ": " + e.getMessage() : ""));
            NotchRuntimeException wrapped = new NotchRuntimeException(runtime.currentStackTrace(), diag, e);
            if (!NotchCatch.catchHandled(runtime, wrapped, clauses)) throw wrapped;
        }
    }
}
