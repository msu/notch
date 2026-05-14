package edu.montana.notch.statements;

import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.chisel.Location;
import edu.montana.notch.chisel.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class NotchForLoop extends NotchStatement {

    public final Token loopVariable;
    public final Token indexVariable;
    public final NotchExpression expr;
    public final List<NotchStatement> loopBody;

    public NotchForLoop(Location start, Token loopVariable, NotchExpression expr, Token indexVariable, List<NotchStatement> loopBody, Location end) {
        super(expr.fileId, start, end);
        this.loopVariable = Objects.requireNonNull(loopVariable);
        this.expr = Objects.requireNonNull(expr);
        this.indexVariable = indexVariable;
        this.loopBody = List.copyOf(Objects.requireNonNull(loopBody));
    }

    @Override
    public void execute(NotchRuntime runtime) {
        Object result = runtime.evaluate(expr);
        int index = 0;
        try (var scope = runtime.pushScope(fileId, span())) {
            result = tryCoerceIterable(result);
            if (result instanceof Iterable<?> i) {
                for (Object o : i) {
                    runtime.defineOrUpdate(((String) loopVariable.data), o);
                    if (indexVariable != null) {
                        runtime.defineOrUpdate(((String) indexVariable.data), index++);
                    }
                    for (NotchStatement notchStatement : loopBody) {
                        runtime.execute(notchStatement);
                    }
                }
            }
        }
    }

    private static Object tryCoerceIterable(Object result) {
        if (result instanceof String s) {
            char[] charArray = s.toCharArray();
            ArrayList<String> charList = new ArrayList<>(charArray.length);
            for (char c : charArray) {
                charList.add(String.valueOf(c));
            }
            result = charList;
        } else if (result instanceof Object[] arr) {
            result = Arrays.asList(arr);
        }
        return result;
    }
}
