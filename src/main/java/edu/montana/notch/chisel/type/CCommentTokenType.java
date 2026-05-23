package edu.montana.notch.chisel.type;

import edu.montana.notch.chisel.*;

public class CCommentTokenType implements TokenType {
    public static final CCommentTokenType C_COMMENT = new CCommentTokenType();

    private CCommentTokenType() {
    }


    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        if (t.take("//")) {
            final var content = t.seek('\n');
            return new TokenData(content);
        }

        final var start = t.location();
        if (t.take("/*")) {
            final var openSpan = new Span(t.source(), start, t.location());
            final var content = t.trySeek("*/");
            if (content == null) {
                final var diag = new Diagnostic();
                diag.highlight(openSpan);
                diag.note("unclosed comment, expected a corresponding */");
                throw new TokenizeException(diag);
            }
            return new TokenData(content);
        }

        return null;
    }
}
