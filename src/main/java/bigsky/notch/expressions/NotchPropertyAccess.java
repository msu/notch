package bigsky.notch.expressions;

import bigsky.notch.runtime.NotchBoundMethod;
import bigsky.notch.runtime.NotchDiagnostic;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.runtime.NotchRuntimeException;
import bigsky.notch.types.*;
import bigsky.utils.Text;
import bigsky.utils.chisel.Location;
import bigsky.utils.chisel.Token;

import java.util.Map;

import static bigsky.notch.runtime.NotchRuntime.UNDEFINED;

public class NotchPropertyAccess extends NotchExpression implements DotPathMember {
    private Token property;
    private NotchExpression root;
    private boolean favorMethods;
    private String dotPath;

    public NotchPropertyAccess(String fileId, Location start, Location end) {
        super(fileId, start, end);
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        Object rootValue = runtime.evaluate(root);
        if (rootValue == null || runtime.isUndefined(rootValue)) {
            return rootValue;
        }

        NotchType runtimeType = TypeSystem.getRuntimeType(rootValue);
        if (favorMethods) {
            NotchBoundMethod method = resolveBoundMethod(rootValue, runtimeType);
            if (method != null) {
                return method;
            } else {
                var error = new NotchDiagnostic();
                error.highlight(fileId, span());
                error.note("no such property/method named %s on %s (%s), no such property".formatted(Text.repr(getProperty()), Text.repr(getParentDotPath()), runtimeType.getDisplayName()));
                throw new NotchRuntimeException(runtime.currentStackTrace(), error);
            }
        }

        if(rootValue instanceof NotchType notchType) {
            NotchProperty staticProperty = notchType.getStaticProperty(property.str());
            if(staticProperty != null) {
                return staticProperty.get(null);
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
            NotchBoundMethod method = resolveBoundMethod(rootValue, runtimeType);
            if (method != null) {
                return method;
            }
        }

        // check if this dot path is an inner class
        if(rootValue instanceof NotchType notchType && isADotPathComponent()) {
            // TODO - move into runtime to support imports
            NotchType type = TypeSystem.getType(notchType.getDisplayName() + "$" + property.str());
            if(type != null) {
                return type;
            }
        }

        return UNDEFINED;
    }

    private NotchBoundMethod resolveBoundMethod(Object rootValue, NotchType runtimeType) {
        if(rootValue instanceof NotchType notchType) {
            NotchMethod staticMethod = notchType.getStaticMethod(property.str());
            if(staticMethod != null) {
                return new NotchBoundMethod(null, staticMethod);
            }
        }

        NotchMethod method = runtimeType.getMethod(property.str());
        if (method != null) {
            return new NotchBoundMethod(rootValue, method);
        }
        return null;
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

    public String getProperty() {
        return property.str();
    }

    public String getParentDotPath() {
        var dp = getDotPath();
        return dp.substring(0, dp.lastIndexOf(DotPathMember.DOT));
    }
}
