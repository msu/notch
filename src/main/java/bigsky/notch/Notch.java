package bigsky.notch;

import bigsky.notch.expressions.NotchExpression;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.statements.NotchStatement;
import bigsky.utils.chisel.Tokenizer;
import bigsky.utils.chisel.type.TokenTypePunct;

import java.io.BufferedInputStream;
import java.io.Console;
import java.nio.file.Path;
import java.sql.SQLOutput;
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
        NotchRuntime notchRuntime = new NotchRuntime();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("notch > ");
            String s = scanner.nextLine();
            NotchParser notchParser = new NotchParser(s);
            try {
                NotchElement elt = notchParser.parse();
                if (elt instanceof NotchExpression expr) {
                    Object result = expr.evaluate(notchRuntime);
                    System.out.println(result);
                } else if (elt instanceof NotchStatement stmt) {
                    stmt.execute(notchRuntime);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
