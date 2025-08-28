package bigsky.notch;

import bigsky.notch.expr.NotchExpression;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.stmt.NotchStatement;
import bigsky.utils.chisel.TokenStream;
import bigsky.utils.chisel.TokenizeException;

public class NotchTestUtils {
    static Object eval(String source) {
        NotchTokenizer tokenizer = new NotchTokenizer(source);
        TokenStream tokens;
        try {
            tokens = tokenizer.tokenize();
        } catch (TokenizeException e) {
            throw new RuntimeException(e);
        }
        NotchParser notchParser = new NotchParser(tokens);
        NotchExpression expr = notchParser.parseExpression();
        Object result = expr.evaluate();
        return result;
    }

    static String exec(String source) {
        NotchTokenizer tokenizer = new NotchTokenizer(source);
        TokenStream tokens;
        try {
            tokens = tokenizer.tokenize();
        } catch (TokenizeException e) {
            throw new RuntimeException(e);
        }
        NotchParser notchParser = new NotchParser(tokens);
        NotchStatement expr = notchParser.parse();
        StringBuilder sb = new StringBuilder();
        NotchRuntime runtime = new NotchRuntime();
        runtime.setOut(obj -> sb.append(obj).append("\n"));
        expr.execute(runtime);
        String result = sb.toString();
        return result;
    }
}
