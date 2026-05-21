package edu.montana.notch.console.commands;

import edu.montana.notch.console.ShellContext;
import org.jline.reader.History;
import picocli.CommandLine.Command;

@Command(name = "history", description = "Show input history.")
public class HistoryCommand implements Runnable {

    private final ShellContext ctx;

    public HistoryCommand(ShellContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run() {
        History history = ctx.reader.getHistory();
        for (History.Entry entry : history) {
            System.out.printf("%4d  %s%n", entry.index(), entry.line());
        }
    }
}
