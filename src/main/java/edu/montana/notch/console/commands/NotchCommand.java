package edu.montana.notch.console.commands;

import edu.montana.notch.chisel.type.*;
import edu.montana.notch.console.token.TokenTypeString;
import edu.montana.notch.NotchElement;
import edu.montana.notch.NotchParser;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.statements.NotchStatement;
import edu.montana.notch.token.NotchTokenTypeKeyword;
import edu.montana.notch.chisel.Location;
import edu.montana.notch.chisel.Spanned;
import edu.montana.notch.chisel.TokenStream;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;
import org.jline.reader.LineReader;

public final class NotchCommand {

    private static final String RUNTIME_VAR = "notchRuntime";

    private NotchCommand() {}

    public static void run(LineReader reader, String line) {
        if (line == null || line.isEmpty()) return;

        NotchRuntime runtime = lookupOrCreateRuntime(reader);

        try {
            Tokenizer tokenizer = new Tokenizer()
                    .withTokenType(NotchTokenTypeKeyword.NOTCH_KEYWORD)
                    .withTokenType(TokenTypeString.STR)
                    .withTokenType(TokenTypeBoolean.BOOL)
                    .withTokenType(TokenTypeIdentifier.IDENT)
                    .withTokenType(TokenTypeInteger.INT)
                    .withTokenType(TokenTypeWhitespace.WHITESPACE)
                    .withTokenTypes(TokenTypePunct.common())
                    .create("jackknife-cli", line);
            TokenStream tokenStream = tokenizer.tokenize();
            NotchParser parser = new NotchParser(tokenStream);
            NotchElement element = parser.parse();
            if (element instanceof NotchExpression expr) {
                Object result = runtime.evaluate(expr);
                System.out.println(result);
            } else if (element instanceof NotchStatement stmt) {
                runtime.execute(stmt);
            }
        } catch (TokenizeException e) {
            System.out.println("error at " + e.start.display() + ": " + e.getMessage());
            printCaret(line, e.start);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            if (e instanceof Spanned s) printCaret(line, s.span().start());
        }
    }

    private static void printCaret(String line, Location loc) {
        if (loc.isSentinel()) return;
        System.out.println("  " + line);
        System.out.println("  " + " ".repeat(Math.max(0, loc.column - 1)) + "^");
    }

    private static NotchRuntime lookupOrCreateRuntime(LineReader reader) {
        Object existing = reader.getVariable(RUNTIME_VAR);
        if (existing instanceof NotchRuntime nr) return nr;
        NotchRuntime created = new NotchRuntime("jackknife-cli");
        reader.setVariable(RUNTIME_VAR, created);
        return created;
    }
}
