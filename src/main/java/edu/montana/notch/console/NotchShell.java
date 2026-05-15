package edu.montana.notch.console;

import edu.montana.notch.console.commands.ClearCommand;
import edu.montana.notch.console.commands.ExitCommand;
import edu.montana.notch.console.commands.LogsCommand;
import edu.montana.notch.console.commands.NotchCommand;
import edu.montana.notch.console.syntaxhighlighter.TerminalSyntaxHighlighter;
import edu.montana.notch.logging.NotchLogging;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Parser;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

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
        CommandLine commandLine = new CommandLine(new RootCommand())
                .addSubcommand("exit",  new ExitCommand(terminal))
                .addSubcommand("clear", new ClearCommand(terminal))
                .addSubcommand("logs",  new LogsCommand())
                .addSubcommand("help",  new CommandLine.HelpCommand())
                .setCaseInsensitiveEnumValuesAllowed(true);

        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .variable(LineReader.HISTORY_SIZE, 500)
                .option(LineReader.Option.CASE_INSENSITIVE, true)
                .option(LineReader.Option.HISTORY_INCREMENTAL, true)
                .highlighter(new TerminalSyntaxHighlighter())
                .completer(new PicocliCompleter(commandLine))
                .build();

        if (!immediate) {
            log.info("Notch: Hit [enter] to start a terminal");
            reader.readLine();
        }
        NotchLogging.getConfig().captureLogsInBackground(() -> {
            reader.printAbove("Logs Available, type 'logs' to show them");
        });

        Parser parser = reader.getParser();
        while (true) {
            String line = reader.readLine("notch > ");
            if (line == null) continue;
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            String[] argv = tokens(parser, line, trimmed);
            if (argv.length > 0 && commandLine.getSubcommands().containsKey(argv[0])) {
                commandLine.execute(argv);
            } else {
                NotchCommand.run(reader, line);
            }
        }
    }

    private static String[] tokens(Parser parser, String line, String fallback) {
        try {
            return parser.parse(line, 0).words().toArray(new String[0]);
        } catch (Exception ignored) {
            return fallback.split("\\s+");
        }
    }

    @CommandLine.Command(name = "notch", description = "Notch interactive shell.")
    static class RootCommand implements Runnable {
        @Override
        public void run() {
            //Root cmd not executed needed for Command Specification
        }
    }
}
