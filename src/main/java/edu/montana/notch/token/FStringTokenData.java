package edu.montana.notch.token;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.*;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.util.BetterList;
import edu.montana.notch.util.Exceptions;

import java.util.List;

/// pieces should be a List<String or NotchExpression>
public record FStringTokenData(List<Object> pieces) {

    public static NotchExpression tokenizeFExpression(Tokenizer t) {
        var start = new Span(t.source(), t.location());

        NotchParser parser;
        NotchExpression expression;
        try {
            var tokens = new BetterList<Token>();
            int open = 1;
            while (true) {
                if (t.atEnd()) {
                    final var diag = new Diagnostic();
                    diag.highlight(start);
                    diag.note("unterminated '{' in f-string here");
                    throw new TokenizeException(diag);
                }

                var token = t.nextToken();
                if (token.type.equals("}")) {
                    open -= 1;
                    if (open == 0) break;
                }

                if (token.type.equals("{")) open += 1;
                tokens.add(token);
            }

            var stream = new TokenStream(t.source(), tokens);
            parser = new NotchParser(stream);
            expression = parser.parseExpression();
        } catch (TokenizeException e) {
            e.diagnostic.note("within f-string");
            e.diagnostic.highlight(start);
            throw Exceptions.rethrow(e);
        }

        if (expression == null) {
            // TODO: embed an error here that is caught but allows tokenization to continue
            final var diag = new Diagnostic();
            diag.highlight(start);
            diag.note("expected an interpolated expression here");
            throw new TokenizeException(diag);
        }

        if (!parser.atEnd()) {
            // TODO: embed an error here that is caught but allows tokenization to continue
            final var diag = new Diagnostic();
            diag.highlight(parser.currentToken());
            diag.note("unexpected extra token here");
            throw new TokenizeException(diag);
        }

        return expression;
    }
}
