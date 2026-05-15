package edu.montana.notch.console.commands;

import org.jline.terminal.Terminal;
import picocli.CommandLine.Command;

@Command(name = "exit", description = "Exit the Notch shell.")
public class ExitCommand implements Runnable {

    private final Terminal terminal;

    public ExitCommand(Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public void run() {
        terminal.writer().println("Goodbye!");
        terminal.flush();
        System.exit(0);
    }
}
