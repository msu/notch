package bigsky.notch.console.commands;

import bigsky.notch.console.token.TokenTypeString;
import bigsky.notch.NotchElement;
import bigsky.notch.NotchParser;
import bigsky.notch.expressions.NotchExpression;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.statements.NotchStatement;
import bigsky.notch.token.NotchTokenTypeKeyword;
import bigsky.notch.chisel.TokenStream;
import bigsky.notch.chisel.TokenizeException;
import bigsky.notch.chisel.Tokenizer;
import bigsky.notch.chisel.type.*;
import bigsky.notch.chisel.type.TokenTypeInteger;
import org.jline.reader.LineReader;

public class NotchCommand implements Command {

    private final LineReader reader;
    private final NotchParser parser;
    private final NotchRuntime runtime;

    private NotchCommand(LineReader reader, TokenStream tokenStream) {
        this.parser = new NotchParser(tokenStream);
        this.reader = reader;

        Object runtime = reader.getVariable("runtime");
        if (runtime instanceof NotchRuntime) {
            this.runtime = (NotchRuntime) runtime;
        } else {
            this.runtime = new NotchRuntime("jackknife-cli");
            reader.setVariable("notchRuntime", this.runtime);
        }
    }

    @Override
    public void execute() {
        try {
            NotchElement notchElement = parser.parse();
            if (notchElement instanceof NotchExpression expr) {
                Object result = runtime.evaluate(expr);
                System.out.println(result);
            } else if (notchElement instanceof NotchStatement stmt) {
                runtime.execute(stmt);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static Command validate(LineReader reader) {
        Object line = reader.getVariable("lastLine");
        String notchCmd = (line == null) ? "" : line.toString();
        try {
            Tokenizer tokenizer = new Tokenizer()
                    .withTokenType(NotchTokenTypeKeyword.NOTCH_KEYWORD)
                    .withTokenType(TokenTypeString.STR)
                    .withTokenType(TokenTypeBoolean.BOOL)
                    .withTokenType(TokenTypeIdentifier.IDENT)
                    .withTokenType(TokenTypeInteger.INT)
                    .withTokenType(TokenTypeWhitespace.WHITESPACE)
                    .withTokenTypes(TokenTypePunct.common())
                    .create("jackknife-cli", notchCmd);
            TokenStream tokenStream = tokenizer.tokenize();
            return new NotchCommand(reader, tokenStream);
        } catch (TokenizeException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
