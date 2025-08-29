package bigsky.notch.statements;

import bigsky.notch.NotchElement;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Location;

public class NotchStatements extends NotchStatement {
    public NotchStatements(Location start, Location end) {
        super(start, end);
    }

    public void addStatement(NotchStatement element) {
        addChild(element);
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
