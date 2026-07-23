package edu.montana.notch.console.commands;

import edu.montana.notch.chisel.Source;
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
            //TODO: Temp Fix need to dig deeper into Jline Parser
            if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                System.out.println("hint: Windows paths with backslashes are mangled by the shell");
                System.out.println("  quote the path:       load \"C:\\Users\\foo\\bar.notch\"");
                System.out.println("  use forward slashes:  load C:/Users/foo/bar.notch");
            }
            return;
        }
        String content = FS.read(fileName);
        final var source = new Source(fileName, content);
        NotchCommand.runWithoutRecording(ctx.reader, source);
        System.out.println("loaded " + fileName);
    }
}
