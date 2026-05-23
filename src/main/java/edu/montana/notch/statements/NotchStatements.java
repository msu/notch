package edu.montana.notch.statements;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;

import java.util.ArrayList;
import java.util.List;

public class NotchStatements extends NotchStatement {
    private final List<NotchStatement> statements;

    public NotchStatements(Span span, ArrayList<NotchStatement> stmts) {
        super(span);
        addChildren(stmts);
        this.statements = stmts;
    }

    @Override
    public void execute(NotchRuntime runtime) {
        for (NotchStatement child : statements) {
            runtime.execute(child);
        }
    }
}
