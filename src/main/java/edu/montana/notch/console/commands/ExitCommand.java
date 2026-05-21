package edu.montana.notch.console.commands;

import edu.montana.notch.console.ShellContext;
import picocli.CommandLine.Command;

@Command(name = "exit", description = "Exit the Notch shell.")
public class ExitCommand implements Runnable {

    private final ShellContext ctx;

    public ExitCommand(ShellContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run() {
        ctx.terminal.writer().println("Goodbye!");
        ctx.terminal.flush();
        System.exit(0);
    }
}
