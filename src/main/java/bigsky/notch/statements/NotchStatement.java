package bigsky.notch.statements;

import bigsky.notch.NotchElement;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Location;

public abstract class NotchStatement extends NotchElement {

    public NotchStatement(String fileId, Location start, Location end) {
        super(fileId, start, end);
    }

    public abstract void execute(NotchRuntime runtime);
}
