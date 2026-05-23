package edu.montana.notch.console.token;

import edu.montana.notch.chisel.TokenData;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;
import edu.montana.notch.chisel.type.IdentTokenType;

import java.util.Set;

public class TerminalKeywordTokenType implements TokenType {
    public static final Set<String> KEYWORDS = Set.of("exit", "help", "clear", "show", "sql");

    public static final TerminalKeywordTokenType TERMINAL_KEYWORD = new TerminalKeywordTokenType();

    private TerminalKeywordTokenType() {
    }

    protected boolean isChar(char c) {
        return Character.isLetterOrDigit(c);
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        final var identData = IdentTokenType.IDENT.tokenize(t);
        if (identData == null) return null;
        //case-sensitive
        if (KEYWORDS.contains(identData.str())) {
            return identData;
        }
        return null;
    }
}
