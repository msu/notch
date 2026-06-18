package edu.montana.notch.console;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.Source;
import edu.montana.notch.chisel.TokenizeException;
import org.jline.reader.EOFError;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.DefaultParser;

public class NotchJLineParser extends DefaultParser {

    public NotchJLineParser() {
        setEofOnUnclosedQuote(true);
        setEofOnUnclosedBracket(Bracket.ROUND, Bracket.SQUARE, Bracket.CURLY);
        setEofOnEscapedNewLine(true);
    }

    @Override
    public ParsedLine parse(String line, int cursor, ParseContext context) {
        ParsedLine parsed = super.parse(line, cursor, context);

        if (context == ParseContext.ACCEPT_LINE && isBlockIncomplete(line)) {
            throw new EOFError(-1, -1, "Missing 'end'", "end");
        }
        return parsed;
    }

    private static boolean isBlockIncomplete(String buffer) {
        var parser = buildParser(buffer);
        if (parser == null) return false;
        try {
            parser.parseAsStatement();
            return false;
        } catch (ParseException e) {
            if (!parser.atEnd()) return false;
            var notes = e.diagnostic.getNotes();
            if (notes.isEmpty()) return false;
            var first = notes.getFirst();
            return first != null && first.startsWith("Unterminated");
        }
    }

    private static NotchParser buildParser(String buffer) {
        try {
            final var src = new Source("repl-incomplete-check", buffer);
            return new NotchParser(src);
        } catch (TokenizeException e) {
            return null;
        }
    }
}
