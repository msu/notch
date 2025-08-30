package bigsky.notch.token;

import bigsky.utils.chisel.Token;
import bigsky.utils.chisel.TokenType;
import bigsky.utils.chisel.TokenizeException;
import bigsky.utils.chisel.Tokenizer;

import java.util.Set;

import static bigsky.utils.chisel.type.TokenTypeIdentifier.IDENT;

public class NotchTokenTypeKeyword implements TokenType {
    public static final NotchTokenTypeKeyword NOTCH_KEYWORD = new NotchTokenTypeKeyword();

    private NotchTokenTypeKeyword() {}

    public static final Set<String> KEYWORDS = Set.of("if", "for", "else", "end");

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
