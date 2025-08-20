package bigsky.notch.stmt;

import bigsky.notch.Location;
import bigsky.notch.NotchToken;
import bigsky.notch.expr.NotchExpression;
import bigsky.notch.runtime.NotchRuntime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ForStatement extends NotchStatement {

    private NotchToken loopVariable;
    private NotchToken indexVariable;
    NotchExpression expr;
    List<NotchStatement> loopBody = new ArrayList<>();

    public ForStatement(Location start, Location end) {
        super(start, end);
    }

    public void setLoopVariable(NotchToken name) {
        this.loopVariable = name;
    }

    public void setIndexVariable(NotchToken indexVariable) {
        this.indexVariable = indexVariable;
    }

    public void setExpression(NotchExpression expr) {
        this.expr = expr;
    }

    public void addLoopBodyStatement(NotchStatement statement) {
        loopBody.add(addChild(statement));
    }

    @Override
    public void execute(NotchRuntime runtime) {
        Object result = expr.evaluate(runtime);
        int index = 0;
        try(var scope = runtime.pushScope()) {
            result = convertResult(result);
            if (result instanceof Iterable i) {
                for (Object o : i) {
                    runtime.setOrInsert(loopVariable.lex, o);
                    if (indexVariable != null) {
                        runtime.setOrInsert(indexVariable.lex, index++);
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
            ArrayList<Character> charList = new ArrayList<>(charArray.length);
            for (char c : charArray) {
                charList.add(c);
            }
            result = charList;
        } else if (result instanceof Object[] arr) {
            result = Arrays.asList(arr);
        }
        return result;
    }
}
