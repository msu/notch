package edu.montana.notch.console;

import edu.montana.notch.Notch;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.TokenStream;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.type.TokenTypeWhitespace;
import edu.montana.notch.token.NotchTokenTypeKeyword;
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
            stream = Notch.TOKENIZER.create("repl-incomplete-check", buffer).tokenize();
        } catch (TokenizeException e) {
            return false;
        }
        int depth = 0;
        for (Token t : stream.toList()) {
            if (t.type == TokenTypeWhitespace.WHITESPACE) continue;
            if (t.type != NotchTokenTypeKeyword.NOTCH_KEYWORD) continue;
            String kw = t.str();
            if ("if".equals(kw) || "for".equals(kw)) depth++;
            else if ("end".equals(kw)) depth--;
        }
        return depth > 0;
    }
}
