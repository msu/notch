package edu.montana.notch.console.commands;

import edu.montana.notch.console.ShellContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Command(name = "write", description = "Toggle recording successful evaluations to a file.")
public class WriteCommand implements Runnable {

    private final ShellContext ctx;

    @Parameters(index = "0", arity = "0..1", paramLabel = "FILE",
            description = "Target file path. Omit to stop recording.")
    String fileName;

    public WriteCommand(ShellContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run() {
        if (fileName == null) {
            stop();
        } else {
            start(fileName);
        }
    }

    private void stop() {
        if (ctx.recording == null) {
            System.out.println("not recording (use 'write <file>' to start)");
            return;
        }
        System.out.printf("stopped recording. wrote %d lines to %s%n",
                ctx.recordingCount, ctx.recording);
        ctx.recording = null;
        ctx.recordingCount = 0;
    }

    private void start(String fileName) {
        if (ctx.recording != null) {
            System.out.printf("already recording to %s; type 'write' to stop first%n",
                    ctx.recording);
            return;
        }
        Path target = Path.of(fileName);
        try {
            Files.writeString(target, "");
        } catch (IOException e) {
            System.out.println("write failed to create file: " + e.getMessage());
            return;
        }
        ctx.recording = target;
        ctx.recordingCount = 0;
        System.out.printf("recording to %s (type 'write' to stop)%n", target);
    }
}
