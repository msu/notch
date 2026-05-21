package edu.montana.notch.console;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import picocli.CommandLine;

import java.nio.file.Path;

public final class ShellContext {
    public final Terminal terminal;
    public LineReader reader;
    public CommandLine commandLine;
    public Path recording;
    public int recordingCount;

    public ShellContext(Terminal terminal) {
        this.terminal = terminal;
    }
}
