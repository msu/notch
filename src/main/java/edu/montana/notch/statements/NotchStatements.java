package edu.montana.notch.statements;

import edu.montana.notch.NotchElement;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.chisel.Location;

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
