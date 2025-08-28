package bigsky.notch.stmt;

import bigsky.notch.NotchElement;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Location;

public abstract class NotchStatement extends NotchElement {

    public NotchStatement(Location start, Location end) {
        super(start, end);
    }

    public abstract void execute(NotchRuntime runtime);
}
