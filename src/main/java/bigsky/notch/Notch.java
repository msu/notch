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

    public static Tokenizer tokenizer(CharSequence source) {
        var out = new Tokenizer(source)
                .with(WHITESPACE)
                .with(BOOL)
                .with(NOTCH_KEYWORD)
                .with(IDENT)
                .with(INT)
                .with(STR)
                .with(TERSE_STRING)
                .with(TokenTypePunct.common());
        return out;
    }

    public static void main(String[] args) {
        NotchRuntime notchRuntime = new NotchRuntime();
        Scanner scanner = new Scanner(System.in);
        while(true) {
            System.out.print("notch > ");
            String s = scanner.nextLine();
            NotchParser notchParser = new NotchParser(tokenizer(s).tokenize());
            try {
                NotchElement elt = notchParser.parse();
                if(elt instanceof NotchExpression expr) {
                    Object result = expr.evaluate(notchRuntime);
                    System.out.println(result);
                } else if(elt instanceof NotchStatement stmt) {
                    stmt.execute(notchRuntime);
                }
            } catch(Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
