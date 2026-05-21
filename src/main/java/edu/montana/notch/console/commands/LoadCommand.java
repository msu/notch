package edu.montana.notch.console.commands;

import edu.montana.notch.console.ShellContext;
import edu.montana.notch.util.FS;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "load", description = "Read and execute a Notch file against the live runtime.")
public class LoadCommand implements Runnable {

    private final ShellContext ctx;

    @Parameters(index = "0", paramLabel = "FILE",
            description = "Notch file to load and execute.")
    String fileName;

    public LoadCommand(ShellContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run() {
        if (!FS.exists(fileName)) {
            System.out.println("file not found: " + fileName);
            return;
        }
        String content = FS.read(fileName);
        NotchCommand.runWithoutRecording(ctx.reader, fileName, content);
        System.out.println("loaded " + fileName);
    }
}
