package edu.montana.notch.console.commands;

import edu.montana.notch.console.ShellContext;
import org.jline.utils.InfoCmp;
import picocli.CommandLine.Command;

@Command(name = "clear", description = "Clear the screen.")
public class ClearCommand implements Runnable {

    private final ShellContext ctx;

    public ClearCommand(ShellContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run() {
        ctx.terminal.puts(InfoCmp.Capability.clear_screen);
        ctx.terminal.flush();
    }
}
