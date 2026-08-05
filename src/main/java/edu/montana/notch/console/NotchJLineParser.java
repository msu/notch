package edu.montana.notch.console;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Source;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.errors.ParserError;
import org.jline.reader.EOFError;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.DefaultParser;

public class NotchJLineParser extends DefaultParser {

    public NotchJLineParser() {
        setEofOnUnclosedQuote(true);
        setEofOnUnclosedBracket(Bracket.ROUND, Bracket.SQUARE, Bracket.CURLY);
        setEofOnEscapedNewLine(true);
        setEscapeChars(null);
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
        parser.parseAsStatement();
        if (!parser.atEnd()) return false;
        for (Diagnostic diag : parser.getDiagnostics()) {
            if (diag.getCode() == ParserError.EP0046) {
                return true;
            }
        }
        return false;
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
