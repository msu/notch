package edu.montana.notch.console;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import picocli.CommandLine;
import picocli.CommandLine.Model.OptionSpec;

import java.util.List;
import java.util.Map;

public class PicocliCompleter implements Completer {

    private final CommandLine root;

    public PicocliCompleter(CommandLine root) {
        this.root = root;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        Map<String, CommandLine> subcommands = root.getSubcommands();

        if (line.wordIndex() == 0) {
            for (String name : subcommands.keySet()) candidates.add(new Candidate(name));
            return;
        }

        String first = line.words().get(0);
        CommandLine sub = subcommands.get(first);
        if (sub == null) return;

        for (OptionSpec opt : sub.getCommandSpec().options()) {
            for (String n : opt.names()) candidates.add(new Candidate(n));
        }
    }
}
