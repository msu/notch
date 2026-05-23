package edu.montana.notch.json5;

import edu.montana.notch.chisel.*;

public class JSON5TokenTypeComment implements TokenType {
    public static final JSON5TokenTypeComment JSON5_COMMENT = new JSON5TokenTypeComment();

    private JSON5TokenTypeComment() {
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        final var start = t.location();

        if (t.take("//")) {
            final var content = t.seek('\n');
            return new TokenData(content);
        }

        if (t.take("/*")) {
            final var content = t.trySeek("*/");
            if (content == null) {
                final var diag = new Diagnostic();
                diag.highlight(new Span(t.source(), start, t.location()));
                diag.note("expected '*/' in tokens");
                throw new TokenizeException(diag);
            }
            return new TokenData(content);
        }

        return null;
    }
}
