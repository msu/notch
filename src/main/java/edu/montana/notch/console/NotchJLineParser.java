package edu.montana.notch.console;

import edu.montana.notch.Notch;
import edu.montana.notch.chisel.Source;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.TokenStream;
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
        TokenStream stream;
        try {
            final var src = new Source("repl-incomplete-check", buffer);
            stream = Notch.TOKENIZER.create(src).tokenize();
        } catch (TokenizeException e) {
            return false;
        }
        int depth = 0;
        for (Token t : stream.toList()) {
            if (t.type.equals("_ws")) continue;
            if (!t.type.equals("keyword")) continue;
            String kw = t.str();
            if ("if".equals(kw) || "for".equals(kw)) depth++;
            else if ("end".equals(kw)) depth--;
        }
        return depth > 0;
    }
}
