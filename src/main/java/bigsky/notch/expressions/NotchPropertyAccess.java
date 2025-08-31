package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchBoundMethod;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.types.*;
import bigsky.utils.chisel.Location;
import bigsky.utils.chisel.Token;

import java.util.Map;

import static bigsky.notch.runtime.NotchRuntime.UNDEFINED;

public class NotchPropertyAccess extends NotchExpression implements DotPathMember {
    private Token property;
    private NotchExpression root;
    private boolean favorMethods;
    private String dotPath;

    public NotchPropertyAccess(Location start, Location end) {
        super(start, end);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object rootValue = root.evaluate(runtime);
        if (rootValue == null) {
            return rootValue;
        } else if (rootValue == UNDEFINED) {
            if(isADotPathComponent()) {
                NotchType type = TypeSystem.getType(dotPath);
                if(type != null) {
                    return type;
                }
            }
            return rootValue;
        }
        NotchType runtimeType = TypeSystem.getRuntimeType(rootValue);
        if (favorMethods) {
            NotchMethod method = runtimeType.getMethod(property.str());
            if (method != null) {
                return new NotchBoundMethod(rootValue, method);
            }
        }

        var notchProp = runtimeType.getProperty(property.str());
        if (notchProp != null) {
            return notchProp.get(rootValue);
        }

        if (rootValue instanceof Map m) {
            return m.get(this.property.str());
        }

        if (rootValue instanceof PropertyMissing pm) {
            Object result = pm.propertyMissing(property.str());
            if (result != UNDEFINED) {
                return result;
            }
        }

        if (!favorMethods) {
            NotchMethod method = runtimeType.getMethod(property.str());
            if (method != null) {
                return new NotchBoundMethod(rootValue, method);
            }
        }

        // TODO - better error handling
        throw new IllegalStateException("The expression " + root + " does not have a property " + property.str() + " on it");
    }

    private boolean isADotPathComponent() {
        return dotPath != null;
    }

    public void setProperty(Token propName) {
        this.property = propName;
    }

    public void setRoot(NotchExpression root) {
        if(root instanceof DotPathMember dpm) {
            String rootDotPath = dpm.getDotPath();
            if (rootDotPath != null) {
                this.dotPath = rootDotPath + DotPathMember.DOT + this.property.str();
            }
        }
        this.root = addChild(root);
    }

    public NotchExpression getRoot() {
        return this.root;
    }

    public void setFavorMethods(boolean favorMethods) {
        this.favorMethods = favorMethods;
    }

    @Override
    public String getDotPath() {
        return this.dotPath;
    }
}
