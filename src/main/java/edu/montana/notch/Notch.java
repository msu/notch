package edu.montana.notch;

import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.statements.NotchStatement;
import edu.montana.notch.chisel.Tokenizer;
import edu.montana.notch.chisel.type.TokenTypePunct;

import java.util.Scanner;

import static edu.montana.notch.token.NotchTokenTypeKeyword.NOTCH_KEYWORD;
import static edu.montana.notch.token.TokenTypeTerseString.TERSE_STRING;
import static edu.montana.notch.chisel.type.TokenTypeBoolean.BOOL;
import static edu.montana.notch.chisel.type.TokenTypeIdentifier.IDENT;
import static edu.montana.notch.chisel.type.TokenTypeInteger.INT;
import static edu.montana.notch.chisel.type.TokenTypeString.STR;
import static edu.montana.notch.chisel.type.TokenTypeWhitespace.WHITESPACE;

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
