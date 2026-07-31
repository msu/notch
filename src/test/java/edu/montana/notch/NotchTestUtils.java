package edu.montana.notch;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.DiagnosticCode;
import edu.montana.notch.chisel.ParseErrors;
import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.Source;
import edu.montana.notch.chisel.TokenStream;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;

import edu.montana.notch.util.BetterList;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
            if (!notchParser.atEnd()) {
                final var diag = new Diagnostic();
                diag.setTitle("TEST:0001");
                diag.note("extra tokens here");
                diag.highlight(notchParser.currentToken());
                throw new RuntimeException(diag.render());
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
            StringBuilder out = new StringBuilder();
            NotchRuntime runtime = new NotchRuntime(source, map(vars));
            runtime.setOut(obj -> out.append(obj).append("\n"));
            Notch.run(source, runtime);
            return out.toString();
        } catch (TokenizeException e) {
            throw new RuntimeException(e.diagnostic.render());
        } catch (NotchRuntimeException e) {
            throw new RuntimeException(e.diagnostic.render());
        }
    }

    public static List<Diagnostic> execDiagnostics(String src, Object... vars) {
        try {
            exec(src, vars);
            return List.of();
        } catch (ParseErrors e) {
            return e.diagnostics;
        }
    }

    public record TestDiagnosticCode(String id, String template) implements DiagnosticCode { }

    public static final DiagnosticCode UNCODED = new TestDiagnosticCode("UNCODED", "(diagnostic with no code)");

    public static void assertCodes(Collection<Diagnostic> diagnostics, DiagnosticCode... expected) {
        var actualIds = BetterList.better(diagnostics)
                .map(diagnostic -> diagnostic.getCode() == null
                        ? UNCODED.id()
                        : diagnostic.getCode().id());
        var expectedIds = new BetterList<>(expected).map(DiagnosticCode::id);
        assertEquals(expectedIds, actualIds,
                () -> "diagnostic codes differ. Diagnostics were:\n" + describe(diagnostics));
    }

    public static String describe(Collection<Diagnostic> diagnostics) {
        if (diagnostics.isEmpty()) return "(none)";
        return BetterList.better(diagnostics)
                .map(diagnostic -> diagnostic.render(false))
                .toString("\n\n");
    }

    public static long countCode(Collection<Diagnostic> diagnostics, DiagnosticCode code) {
        return BetterList.better(diagnostics)
                .filter(diagnostic -> diagnostic.getCode() == code)
                .count();
    }

    public static long countCode(Throwable failure, DiagnosticCode code) {
        return failure instanceof ParseErrors parseErrors
                ? countCode(parseErrors.diagnostics, code)
                : 0;
    }

    public static boolean hasCode(Throwable failure, DiagnosticCode code) {
        return countCode(failure, code) > 0;
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
