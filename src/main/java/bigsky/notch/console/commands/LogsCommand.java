package bigsky.notch.console.commands;

import bigsky.notch.logging.NotchLogging;
import org.jline.reader.History;
import org.jline.reader.LineReader;

public class LogsCommand implements Command {

    private LogsCommand (LineReader reader) {}

    @Override
    public void execute() {
        System.out.println(NotchLogging.getConfig().takeBackgroundLogs());
    }

    public static Command validate(LineReader reader) {
        Object line = reader.getVariable("lastLine");
        String lastLine = (line == null) ? "" : line.toString();
        if (lastLine.split(" ")[0].equalsIgnoreCase("logs")) {
            return new LogsCommand(reader);
        }
        return null;
    }
}
