package edu.montana.notch.token;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;

import java.util.Set;

import static edu.montana.notch.chisel.type.TokenTypeIdentifier.IDENT;

public class NotchTokenTypeKeyword implements TokenType {
    public static final NotchTokenTypeKeyword NOTCH_KEYWORD = new NotchTokenTypeKeyword();

    private NotchTokenTypeKeyword() {}

    public static final Set<String> KEYWORDS = Set.of("if", "for", "else", "end", "in", "is", "not", "null", "as");

    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
        var ident = IDENT.tokenize(t);
        if (ident == null) return null;
        if (KEYWORDS.contains(ident.str())) {
            return ident.withType(this);
        } else {
            return null;
        }
    }
}
