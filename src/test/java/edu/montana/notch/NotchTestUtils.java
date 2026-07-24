package edu.montana.notch;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.Source;
import edu.montana.notch.chisel.TokenStream;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.statements.NotchStatement;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class NotchTestUtils {
    public static RuntimeException evalEx(String src, Object... vars) {
        return assertThrows(RuntimeException.class, () -> eval(src, vars));
    }

    public static Object eval(String src, Object... vars) {
        try {
            Source source = new Source("<eval>", src);
            TokenStream tokens = Notch.TOKENIZER.tokenize(source);
            NotchParser notchParser = new NotchParser(tokens);
            NotchExpression expr = notchParser.parseExpression();
            if (notchParser.hasErrors()) {
                StringBuilder parseErrorMessage = new StringBuilder("Parse errors evaluating: " + src);
                for (Diagnostic diagnostic : notchParser.getDiagnostics()) {
                    parseErrorMessage.append("\n").append(diagnostic.render());
                }
                throw new RuntimeException(parseErrorMessage.toString());
            }
            Object result = expr.evaluate(map(vars));
            return result;
        } catch (NotchRuntimeException ex) {
            var result = ex.getMessage();
            throw new RuntimeException(result);
        } catch (ParseException ex) {
            var result = ex.getMessage();
            throw new RuntimeException(result);
        } catch (TokenizeException ex) {
            var result = ex.getMessage();
            throw new RuntimeException(result);
        }
    }

    public static Object evalNoCatch(String src, Object... vars) {
        final Source source = new Source("<eval", src);
        TokenStream tokens = Notch.TOKENIZER.tokenize(source);
        NotchParser notchParser = new NotchParser(tokens);
        NotchExpression expr = notchParser.parseExpression();
        return expr.evaluate(map(vars));
    }

    public static String exec(String src, Object... vars) {
        final Source source = new Source("<exec>", src);
        try {
            TokenStream tokens = Notch.TOKENIZER.tokenize(source);
            NotchParser notchParser = new NotchParser(tokens);
            NotchStatement stmt = notchParser.parseAsStatement();
            if (notchParser.hasErrors()) {
                StringBuilder parseErrorMessage = new StringBuilder("Parse errors executing: " + src);
                for (Diagnostic diagnostic : notchParser.getDiagnostics()) {
                    parseErrorMessage.append("\n").append(diagnostic.render());
                }
                throw new RuntimeException(parseErrorMessage.toString());
            }
            StringBuilder out = new StringBuilder();
            NotchRuntime runtime = new NotchRuntime(source, map(vars));
            runtime.setOut(obj -> out.append(obj).append("\n"));
            runtime.execute(stmt);
            return out.toString();
        } catch (TokenizeException e) {
            throw new RuntimeException(e.diagnostic.render());
        } catch (NotchRuntimeException e) {
            throw new RuntimeException(e.diagnostic.render());
        }
    }

    public static Map<String, Object> map(Object[] vars) {
        Map<String, Object> map = Notch.defaultGlobals();
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
