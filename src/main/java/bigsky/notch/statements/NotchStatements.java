package bigsky.notch.statements;

import bigsky.notch.NotchElement;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Location;

import java.util.List;

public class NotchStatements extends NotchStatement {
    public NotchStatements(Location start, Location end) {
        super(start, end);
    }
    @Override
    public void execute(NotchRuntime runtime) {
        for (NotchElement child : getChildren()) {
            if(child instanceof NotchStatement ns) {
                ns.execute(runtime);
            }
        }
    }
}
