package bigsky.notch.statements;

import bigsky.notch.expressions.NotchExpression;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.utils.chisel.Location;
import bigsky.utils.chisel.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotchForLoop extends NotchStatement {

    private Token loopVariable;
    private Token indexVariable;
    NotchExpression expr;
    List<NotchStatement> loopBody = new ArrayList<>();

    public NotchForLoop(Location start, Location end) {
        super(start, end);
    }

    public void setLoopVariable(Token name) {
        this.loopVariable = name;
    }

    public void setIndexVariable(Token indexVariable) {
        this.indexVariable = indexVariable;
    }

    public void setExpression(NotchExpression expr) {
        this.expr = addChild(expr);
    }

    public void setLoopBody(List<NotchStatement> statements) {
        this.loopBody = addChildren(statements);
    }

    @Override
    public void execute(NotchRuntime runtime) {
        Object result = expr.evaluate(runtime);
        int index = 0;
        try(var scope = runtime.pushScope()) {
            result = convertResult(result);
            if (result instanceof Iterable i) {
                for (Object o : i) {
                    runtime.defineOrUpdate(((String) loopVariable.data), o);
                    if (indexVariable != null) {
                        runtime.defineOrUpdate(((String) indexVariable.data), index++);
                    }
                    for (NotchStatement notchStatement : loopBody) {
                        notchStatement.execute(runtime);
                    }
                }
            }
        }
    }

    private static Object convertResult(Object result) {
        if(result instanceof String s) {
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
