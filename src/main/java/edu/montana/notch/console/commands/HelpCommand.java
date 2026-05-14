package edu.montana.notch.console.commands;

import org.jline.reader.LineReader;

public class HelpCommand implements Command {

    private HelpCommand(LineReader reader) {}

    @Override
    public void execute() {
        System.out.println("\nAvailable Commands:");
        System.out.println("  help     - Show this help message");
        System.out.println("  clear    - Clear the screen");
        System.out.println("  exit     - Exit the terminal");
        System.out.println("\nSQL Commands:");
        System.out.println("  SELECT, INSERT, UPDATE, DELETE, etc.");
        System.out.println("  Use TAB for auto-completion");
        System.out.println("  Use UP/DOWN arrows for command history");
        System.out.println();
    }

    public static Command validate(LineReader reader) {
        Object line = reader.getVariable("lastLine");
        String lastLine = (line == null) ? "" : line.toString();
        if (lastLine.split(" ")[0].equalsIgnoreCase("help")) {
            return new HelpCommand(reader);
        }
        return null;
    }
}