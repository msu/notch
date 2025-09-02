package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.types.NotchProperty;
import bigsky.notch.types.NotchType;
import bigsky.notch.types.TypeSystem;
import bigsky.utils.chisel.Location;

import java.util.List;
import java.util.Map;

public class NotchIndexExpression extends NotchExpression {
    NotchExpression root;
    NotchExpression value;

    public NotchIndexExpression(Location start, Location end) {
        super(start, end);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object rootValue = root.evaluate(runtime);
        Object index = value.evaluate(runtime);
        if(rootValue == null) {
            return null;
        }
        // TODO this needs to be a lot more flexible
        if(rootValue instanceof Map m) {
            return m.get(index);
        } else if(rootValue instanceof List l) {
            return l.get(toInteger(index));
        } else if(rootValue instanceof Object[] arr) {
            return arr[toInteger(index)];
        } else if(rootValue instanceof String s) {
            return String.valueOf(s.charAt(toInteger(index)));
        } else {
            NotchType runtimeType = TypeSystem.getRuntimeType(rootValue);
            NotchProperty property = runtimeType.getProperty(String.valueOf(index));
            if(property != null) {
                return property.get(rootValue);
            }
        }
        return null;
    }

    private static Integer toInteger(Object index) {
        // TODO - replace with proper coercions
        return (Integer) index;
    }

    public void setRoot(NotchExpression root) {
        this.root = addChild(root);
    }

    public void setValue(NotchExpression value) {
        this.value = addChild(value);
    }
}
