package edu.montana.notch;

import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.Source;
import edu.montana.notch.chisel.TokenStream;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.statements.NotchStatement;

import java.util.HashMap;
import java.util.Map;

public class NotchTestUtils {
    public static Object eval(String src, Object... vars) {
        try {
            Source source = new Source("<eval>", src);
            TokenStream tokens = Notch.TOKENIZER.tokenize(source);
            NotchParser notchParser = new NotchParser(tokens);
            NotchExpression expr = notchParser.parseExpression();
            Object result = expr.evaluate(map(vars));
            return result;
        } catch (NotchRuntimeException ex) {
            var result = ex.diagnostic.render();
            throw new RuntimeException(result);
        } catch (ParseException ex) {
            var result = ex.diagnostic.render();
            throw new RuntimeException(result);
        } catch (TokenizeException ex) {
            var result = ex.diagnostic.render();
            throw new RuntimeException(result);
        }
    }

    public static Object evalNoCatch(String src, Object... vars) {
        final Source source = new Source("<eval", src);
        TokenStream tokens = Notch.TOKENIZER.tokenize(source);
        NotchParser notchParser = new NotchParser(tokens);
        NotchExpression expr = notchParser.parseExpression();
        Object result = expr.evaluate(map(vars));
        return result;
    }

    public static String exec(String src, Object... vars) {
        final Source source = new Source("<exec>", src);
        TokenStream tokens;
        try {
            tokens = Notch.TOKENIZER.tokenize(source);
            NotchParser notchParser = new NotchParser(tokens);
            NotchStatement expr = notchParser.parseAsStatement();
            StringBuilder sb = new StringBuilder();
            NotchRuntime runtime = new NotchRuntime(source, map(vars));
            runtime.setOut(obj -> sb.append(obj).append("\n"));
            runtime.execute(expr);
            String result = sb.toString();
            return result;
        } catch (TokenizeException e) {
            throw new RuntimeException(e.diagnostic.render());
        } catch (ParseException e) {
            throw new RuntimeException(e.diagnostic.render());
        } catch (NotchRuntimeException e) {
            throw new RuntimeException(e.diagnostic.render());
        }
    }

    public static Map<String, Object> map(Object[] vars) {
        HashMap<String, Object> map = new HashMap<>();
        for (int i = 0; i < vars.length; i++) {
            Object key = vars[i];
            Object val = null;
            if (++i < vars.length) {
                val = vars[i];
            }
            map.put(String.valueOf(key), val);
        }
        return map;
    }
}
