package bigsky.notch.statements;

import bigsky.notch.NotchElement;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Location;

public class NotchStatements extends NotchStatement {
    public NotchStatements(String fileId, Location start, Location end) {
        super(fileId, start, end);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        for (NotchElement child : getChildren()) {
            if(child instanceof NotchStatement ns) {
                runtime.execute(ns);
            }
        }
    }
}
