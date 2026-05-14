package edu.montana.notch.console.token;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;

import java.util.Set;

public class TokenTypeTerminalKeyword implements TokenType {
    public static final Set<String> KEYWORDS = Set.of("exit", "help", "clear", "show", "sql");

    public static final TokenTypeTerminalKeyword TERMINAL_KEYWORD = new TokenTypeTerminalKeyword();

    private TokenTypeTerminalKeyword() {}

    protected boolean isChar(char c) {
        return Character.isLetterOrDigit(c);
    }

    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
        var start = t.location();
        var content = new StringBuilder();
        do content.append(t.take());
        while (isChar(t.peek()));
        //case-sensitive
        if (KEYWORDS.contains(content.toString())) {
            return new Token(start, t.location(), this, content.toString());
        }
        return null;
    }
}
