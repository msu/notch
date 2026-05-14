package edu.montana.notch.statements;

import edu.montana.notch.NotchElement;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.chisel.Location;

public abstract class NotchStatement extends NotchElement {

    public NotchStatement(String fileId, Location start, Location end) {
        super(fileId, start, end);
    }

    public abstract void execute(NotchRuntime runtime);
}
