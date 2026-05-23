package edu.montana.notch.statements;

import edu.montana.notch.NotchElement;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;

public abstract class NotchStatement extends NotchElement {

    public NotchStatement(Span span) {
        super(span);
    }

    public abstract void execute(NotchRuntime runtime);
}
