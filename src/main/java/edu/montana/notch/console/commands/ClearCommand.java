package edu.montana.notch.console.commands;

import org.jline.utils.InfoCmp;
import org.jline.reader.LineReader;

public class ClearCommand implements Command {

    private final LineReader reader;

    private ClearCommand(LineReader reader) {
        this.reader = reader;
    }

    public static Command validate(LineReader reader) {
        Object line = reader.getVariable("lastLine");
        String lastLine = (line == null) ? "" : line.toString();
        if (lastLine.split(" ")[0].equalsIgnoreCase("clear")) {
            return new ClearCommand(reader);
        }
        return null;
    }

    public void execute() {
        reader.getTerminal().puts(InfoCmp.Capability.clear_screen);
        reader.getTerminal().flush();
    }

}
