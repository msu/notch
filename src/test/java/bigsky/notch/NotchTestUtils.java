package bigsky.notch;

import bigsky.notch.expressions.NotchExpression;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.statements.NotchStatement;
import bigsky.utils.chisel.TokenStream;
import bigsky.utils.chisel.TokenizeException;

import java.util.HashMap;
import java.util.Map;

import static bigsky.utils.Exceptions.rethrow;

public class NotchTestUtils {
    static Object eval(String source, Object... vars) {
        NotchTokenizer tokenizer = new NotchTokenizer(source);
        TokenStream tokens;
        try {
            tokens = tokenizer.tokenize();
        } catch (TokenizeException e) {
            throw rethrow(e);
        }
        NotchParser notchParser = new NotchParser(tokens);
        NotchExpression expr = notchParser.parseExpression();
        Object result = expr.evaluate(map(vars));
        return result;
    }

    static String exec(String source, Object... vars) {
        NotchTokenizer tokenizer = new NotchTokenizer(source);
        TokenStream tokens;
        try {
            tokens = tokenizer.tokenize();
        } catch (TokenizeException e) {
            throw rethrow(e);
        }
        NotchParser notchParser = new NotchParser(tokens);
        NotchStatement expr = notchParser.parse();
        StringBuilder sb = new StringBuilder();
        NotchRuntime runtime = new NotchRuntime(map(vars));
        runtime.setOut(obj -> sb.append(obj).append("\n"));
        expr.execute(runtime);
        String result = sb.toString();
        return result;
    }

    private static Map<String, Object> map(Object[] vars) {
        HashMap<String, Object> map = new HashMap<>();
        for (int i = 0; i < vars.length; i++) {
            Object key = vars[i];
            Object val = null;
            if(++i < vars.length) {
                val = vars[i];
            }
            map.put(String.valueOf(key), val);
        }
        return map;
    }

}
