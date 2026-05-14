package edu.montana.notch.console;

import edu.montana.notch.console.commands.ClearCommand;
import edu.montana.notch.console.commands.Command;
import edu.montana.notch.console.commands.ExitCommand;
import edu.montana.notch.console.commands.HelpCommand;
import edu.montana.notch.console.commands.LogsCommand;
import edu.montana.notch.console.commands.NotchCommand;
import edu.montana.notch.console.syntaxhighlighter.TerminalSyntaxHighlighter;
import edu.montana.notch.logging.NotchLogging;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static edu.montana.notch.util.Exceptions.safelyEval;

public class NotchShell {
    private static final Logger log = LoggerFactory.getLogger(NotchShell.class);
    private static NotchShell INST = null;

    private final boolean immediate;

    public NotchShell(boolean immediate) {
        this.immediate = immediate;
    }

    public static void start(boolean immediate) {
        if (INST == null) {
            Attributes attributes = new Attributes();
            attributes.setLocalFlag(Attributes.LocalFlag.ISIG, true);
            var shell = new NotchShell(immediate);
            Terminal terminal = safelyEval(() -> TerminalBuilder.builder()
                    .system(true)
                    .attributes(attributes)
                    .build());

            if (terminal.getType().equals(Terminal.TYPE_DUMB)) log.warn("Terminal type is Dumb");

            shell.startJLineTerminal(terminal);
            INST = shell;
        }
    }

    public static void main(String[] args) {
        start(true);
    }

    private void startJLineTerminal(Terminal terminal) {
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .variable(LineReader.HISTORY_SIZE, 500)
                .option(LineReader.Option.CASE_INSENSITIVE, true)
                .option(LineReader.Option.HISTORY_INCREMENTAL, true)
                .highlighter(new TerminalSyntaxHighlighter())
                .build();

        if (!immediate) {
            log.info("Notch: Hit [enter] to start a terminal");
            reader.readLine();
        }
        NotchLogging.getConfig().captureLogsInBackground(() -> {
            reader.printAbove("Logs Available, type 'logs' to show them");
        });

        String line;
        Command command;

        do {
            line = reader.readLine("notch > ");
            reader.setVariable("lastLine", line);
            Command[] commands = {
                    ExitCommand.validate(reader),
                    ClearCommand.validate(reader),
                    LogsCommand.validate(reader),
                    HelpCommand.validate(reader),
                    NotchCommand.validate(reader)
            };

            int i = 0;
            do {
                command = commands[i];
            }
            while (command == null && ++i < commands.length);
            if (command == null && !line.isEmpty()) terminal.writer().println("Command Not Found");
            if (command != null) command.execute();
        } while (!(command instanceof ExitCommand));
        System.exit(0);
    }
}
