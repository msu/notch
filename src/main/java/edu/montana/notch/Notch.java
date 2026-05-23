package edu.montana.notch;

import edu.montana.notch.chisel.Tokenizer;
import edu.montana.notch.chisel.type.LiteralTokenType;

import static edu.montana.notch.chisel.type.BooleanTokenType.BOOL;
import static edu.montana.notch.chisel.type.CStringTokenType.STR;
import static edu.montana.notch.chisel.type.IdentTokenType.IDENT;
import static edu.montana.notch.chisel.type.IntegerTokenType.INT;
import static edu.montana.notch.chisel.type.WhitespaceTokenType.WHITESPACE;
import static edu.montana.notch.token.NotchTokenTypeKeyword.NOTCH_KEYWORD;
import static edu.montana.notch.token.TerseStringTokenType.TERSE_STRING;

public class Notch {
    private Notch() {
    }

    public static final Tokenizer TOKENIZER = new Tokenizer()
            .withTokenType("_ws", WHITESPACE)
            .withTokenType("bool", BOOL)
            .withTokenType("keyword", NOTCH_KEYWORD)
            .withTokenType("ident", IDENT)
            .withTokenType("int", INT)
            .withTokenType("string", STR)
            .withTokenType("string", TERSE_STRING)
            .withTokenTypes(LiteralTokenType.COMMON);

    public static void main(String[] args) {
        // TODO: custom source
        // NotchRuntime notchRuntime = new NotchRuntime("notch-repl");
        // Scanner scanner = new Scanner(System.in);
        // while (true) {
        //     System.out.print("notch > ");
        //     String s = scanner.nextLine();
        //     NotchParser notchParser = new NotchParser("notch-repl", s);
        //     try {
        //         NotchElement elt = notchParser.parse();
        //         if (elt instanceof NotchExpression expr) {
        //             Object result = notchRuntime.evaluate(expr);
        //             System.out.println(result);
        //         } else if (elt instanceof NotchStatement stmt) {
        //             notchRuntime.execute(stmt);
        //         }
        //     } catch (Exception e) {
        //         System.out.println(e.getMessage());
        //     }
        // }
    }
}
