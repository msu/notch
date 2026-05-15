package edu.montana.notch.console.commands;

import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;
import picocli.CommandLine.Command;

@Command(name = "clear", description = "Clear the screen.")
public class ClearCommand implements Runnable {

    private final Terminal terminal;

    public ClearCommand(Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public void run() {
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.flush();
    }
}
