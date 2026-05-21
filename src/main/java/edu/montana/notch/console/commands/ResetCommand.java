package edu.montana.notch.console.commands;

import edu.montana.notch.console.ShellContext;
import picocli.CommandLine.Command;

@Command(name = "reset", description = "Wipe the runtime and start fresh.")
public class ResetCommand implements Runnable {

    private final ShellContext ctx;

    public ResetCommand(ShellContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run() {
        NotchCommand.resetRuntime(ctx.reader);
        System.out.println("runtime reset");
    }
}
