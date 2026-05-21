package edu.montana.notch.console.commands;

import edu.montana.notch.console.ShellContext;
import org.jline.reader.History;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@Command(name = "save", description = "Save session inputs to a file.")
public class SaveCommand implements Runnable {

    private final ShellContext ctx;

    @Parameters(index = "0", paramLabel = "FILE", description = "Target file path.")
    String fileName;

    public SaveCommand(ShellContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run() {
        Set<String> subNames = ctx.commandLine.getSubcommands().keySet();
        Path target = Path.of(fileName);
        int written = 0;
        try (BufferedWriter writer = Files.newBufferedWriter(target)) {
            History history = ctx.reader.getHistory();
            for (History.Entry entry : history) {
                String line = entry.line();
                if (subNames.contains(firstWord(line))) continue;
                writer.write(line);
                writer.newLine();
                written++;
            }
            System.out.printf("saved %d lines to %s%n", written, fileName);
        } catch (IOException e) {
            System.out.println("save failed: " + e.getMessage());
        }
    }

    private static String firstWord(String line) {
        String trimmed = line.trim();
        int space = trimmed.indexOf(' ');
        return space < 0 ? trimmed : trimmed.substring(0, space);
    }
}
