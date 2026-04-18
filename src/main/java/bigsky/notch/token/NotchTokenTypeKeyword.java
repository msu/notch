package bigsky.notch.token;

import bigsky.notch.chisel.Token;
import bigsky.notch.chisel.TokenType;
import bigsky.notch.chisel.TokenizeException;
import bigsky.notch.chisel.Tokenizer;

import java.util.Set;

import static bigsky.notch.chisel.type.TokenTypeIdentifier.IDENT;

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
