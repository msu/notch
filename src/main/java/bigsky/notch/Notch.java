package bigsky.notch;

import bigsky.notch.expressions.NotchExpression;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.statements.NotchStatement;
import bigsky.utils.chisel.Tokenizer;
import bigsky.utils.chisel.type.TokenTypePunct;

import java.util.Scanner;

import static bigsky.notch.token.NotchTokenTypeKeyword.NOTCH_KEYWORD;
import static bigsky.notch.token.TokenTypeTerseString.TERSE_STRING;
import static bigsky.utils.chisel.type.TokenTypeBoolean.BOOL;
import static bigsky.utils.chisel.type.TokenTypeIdentifier.IDENT;
import static bigsky.utils.chisel.type.TokenTypeInteger.INT;
import static bigsky.utils.chisel.type.TokenTypeString.STR;
import static bigsky.utils.chisel.type.TokenTypeWhitespace.WHITESPACE;

public class Notch {
    private Notch() {
    }

    public static final Tokenizer TOKENIZER = new Tokenizer()
            .withTokenType(WHITESPACE)
            .withTokenType(BOOL)
            .withTokenType(NOTCH_KEYWORD)
            .withTokenType(IDENT)
            .withTokenType(INT)
            .withTokenType(STR)
            .withTokenType(TERSE_STRING)
            .withTokenTypes(TokenTypePunct.common());

    public static void main(String[] args) {
        NotchRuntime notchRuntime = new NotchRuntime("notch-repl");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("notch > ");
            String s = scanner.nextLine();
            NotchParser notchParser = new NotchParser("notch-repl", s);
            try {
                NotchElement elt = notchParser.parse();
                if (elt instanceof NotchExpression expr) {
                    Object result = notchRuntime.evaluate(expr);
                    System.out.println(result);
                } else if (elt instanceof NotchStatement stmt) {
                    notchRuntime.execute(stmt);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
