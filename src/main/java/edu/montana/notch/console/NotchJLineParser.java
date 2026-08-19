package edu.montana.notch.console;

import edu.montana.notch.Notch;
import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Source;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.errors.ParserError;
import org.jline.reader.EOFError;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.DefaultParser;

import java.util.List;

public class NotchJLineParser extends DefaultParser {

    private static final String COMMENT_TOKEN_TYPE = "_comment";

    private static final String BLOCK_COMMENT_CLOSER = "\n*/";

    public NotchJLineParser() {
        setEofOnUnclosedQuote(true);
        setEofOnUnclosedBracket(Bracket.ROUND, Bracket.SQUARE, Bracket.CURLY);
        setEofOnEscapedNewLine(true);
        setEscapeChars(null);
    }

    @Override
    public ParsedLine parse(String line, int cursor, ParseContext context) {
        ParsedLine parsed = super.parse(maskComments(line), cursor, context);

        if (context == ParseContext.ACCEPT_LINE && isBlockIncomplete(line)) {
            throw new EOFError(-1, -1, "Missing 'end'", "end");
        }
        return parsed;
    }

    private static String maskComments(String line) {
        final List<Token> tokens;
        try {
            tokens = Notch.TOKENIZER.tokenize(new Source("repl-comment-mask", line)).toList();
        } catch (TokenizeException e) {
            return line;
        }

        char[] masked = null;
        for (Token token : tokens) {
            if (!COMMENT_TOKEN_TYPE.equals(token.type)) continue;
            if (masked == null) masked = line.toCharArray();

            int from = Math.max(token.span.start().index, 0);
            int to = Math.min(token.span.end().index, masked.length);
            for (int i = from; i < to; i++) {
                if (masked[i] != '\n') masked[i] = ' ';
            }
        }
        return masked == null ? line : new String(masked);
    }

    private static boolean isBlockIncomplete(String buffer) {
        var parser = buildParser(buffer);
        if (parser == null) return hasUnclosedBlockComment(buffer);
        parser.parseAsStatement();
        if (!parser.atEnd()) return false;
        for (Diagnostic diag : parser.getDiagnostics()) {
            if (diag.getCode() == ParserError.EP0046) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasUnclosedBlockComment(String buffer) {
        try {
            Notch.TOKENIZER.tokenize(new Source("repl-unclosed-comment-check", buffer + BLOCK_COMMENT_CLOSER));
            return true;
        } catch (TokenizeException e) {
            return false;
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
