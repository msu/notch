package bigsky.notch.console.commands;

import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

public class ExitCommand implements Command{


    private final LineReader reader;

    private ExitCommand(LineReader reader) {this.reader = reader;}

    public void execute() {
        Terminal terminal = reader.getTerminal();
        terminal.writer().println("Goodbye!");
        terminal.flush();
    }

    public static Command validate(LineReader reader) {
        Object line = reader.getVariable("lastLine");
        String lastLine = (line == null) ? "" : line.toString();
        if (lastLine.split(" ")[0].equalsIgnoreCase("exit")) {
            return new ExitCommand(reader);
        }
        return null;
    }
}
