package edu.montana.notch.runtime;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.util.Text;

public class UnknownVariableException extends NotchRuntimeException {
    final Token token;

    public UnknownVariableException(Token token, NotchStackTrace stackTrace) {
        super(stackTrace, new Diagnostic()
                .highlight(token)
                .note("unknown variable %s".formatted(Text.repr(token.str()))));
        this.token = token;

    }

    public String variableName() {
        return token.str();
    }
}
