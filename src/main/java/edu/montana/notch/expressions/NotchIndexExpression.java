package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Location;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.NotchProperty;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.TypeSystem;

import java.util.List;
import java.util.Map;

public class NotchIndexExpression extends NotchExpression {
    public final NotchExpression root;
    public final NotchExpression index;

    public NotchIndexExpression(NotchExpression root, NotchExpression index, Location end) {
        super(root.span.through(end));
        this.root = root;
        this.index = index;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object rootValue = runtime.evaluate(root);
        Object indexValue = runtime.evaluate(index);
        if (rootValue == null) {
            return null;
        }
        // TODO this needs to be a lot more flexible
        if (rootValue instanceof Map m) {
            return m.get(indexValue);
        } else if (rootValue instanceof List l) {
            return l.get(toInteger(indexValue));
        } else if (rootValue instanceof Object[] arr) {
            return arr[toInteger(indexValue)];
        } else if (rootValue instanceof String s) {
            return String.valueOf(s.charAt(toInteger(indexValue)));
        } else {
            NotchType runtimeType = TypeSystem.getRuntimeType(rootValue);
            NotchProperty property = runtimeType.getProperty(String.valueOf(indexValue));
            if (property != null) {
                return property.get(rootValue);
            }
        }
        return null;
    }

    private static Integer toInteger(Object index) {
        // TODO - replace with proper coercions
        return (Integer) index;
    }
}
