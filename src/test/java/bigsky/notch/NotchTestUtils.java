package bigsky.notch;

import bigsky.notch.expr.NotchExpression;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.stmt.NotchStatement;

public class NotchTestUtils {
    static Object eval(String source) {
        NotchParser notchParser = new NotchParser(source);
        NotchExpression expr = notchParser.parseExpression();
        Object result = expr.evaluate();
        return result;
    }

    static String exec(String source) {
        NotchParser notchParser = new NotchParser(source);
        NotchStatement expr = notchParser.parse();
        StringBuilder sb = new StringBuilder();
        NotchRuntime runtime = new NotchRuntime();
        runtime.setOut(obj -> sb.append(obj).append("\n"));
        expr.execute(runtime);
        String result = sb.toString();
        return result;
    }
}
