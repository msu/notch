package bigsky.notch;

import bigsky.notch.expressions.NotchExpression;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.runtime.NotchRuntimeException;
import bigsky.notch.runtime.SourceProvider;
import bigsky.notch.statements.NotchStatement;
import bigsky.notch.chisel.Span;
import bigsky.notch.chisel.TokenStream;
import bigsky.notch.chisel.TokenizeException;

import java.util.HashMap;
import java.util.Map;

import static bigsky.notch.util.Exceptions.rethrow;

public class NotchTestUtils {
    public static Object eval(String source, Object... vars) {
        TokenStream tokens;
        try {
            tokens = Notch.TOKENIZER.tokenize("<eval>", source);
        } catch (TokenizeException e) {
            throw rethrow(e);
        }
        NotchParser notchParser = new NotchParser(tokens);
        NotchExpression expr = notchParser.parseExpression();
        try {
            Object result = expr.evaluate(map(vars));
            return result;
        } catch (NotchRuntimeException ex) {
            var result = ex.diagnostic.render((fileId, span) -> {
                var start = span.start().index;
                var end = span.end().index;
                return source.substring(start, Math.min(source.length(), end));
            });
            throw new RuntimeException(result);
        }
    }

    public static Object evalNoCatch(String source, Object... vars) {
        TokenStream tokens;
        try {
            tokens = Notch.TOKENIZER.tokenize("<eval>", source);
        } catch (TokenizeException e) {
            throw rethrow(e);
        }
        NotchParser notchParser = new NotchParser(tokens);
        NotchExpression expr = notchParser.parseExpression();
        Object result = expr.evaluate(map(vars));
        return result;
    }

    public static String exec(String source, Object... vars) {
        TokenStream tokens;
        try {
            tokens = Notch.TOKENIZER.tokenize("<exec>", source);
        } catch (TokenizeException e) {
            throw rethrow(e);
        }
        NotchParser notchParser = new NotchParser(tokens);
        NotchStatement expr = notchParser.parseAsStatement();
        StringBuilder sb = new StringBuilder();
        NotchRuntime runtime = new NotchRuntime("<exec>", map(vars));
        runtime.setOut(obj -> sb.append(obj).append("\n"));
        runtime.execute(expr);
        String result = sb.toString();
        return result;
    }

    public static Map<String, Object> map(Object[] vars) {
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
