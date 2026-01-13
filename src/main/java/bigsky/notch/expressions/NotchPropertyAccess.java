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
            return method;
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

        var error = new NotchDiagnostic();
        error.highlight(fileId, span());
        String hint;
        if (rootValue instanceof NotchType notchType) {
            hint = resolveClosestFeatureName(notchType);
        } else {
            hint = resolveClosestFeatureName(runtimeType);
        }
        error.note("no property/method named %s on %s (%s)%s".formatted(Text.repr(getProperty()),
                Text.repr(getParentDotPath()),
                runtimeType.getDisplayName(), hint));
        throw new NotchRuntimeException(runtime.currentStackTrace(), error);

    }

    private String resolveClosestFeatureName(NotchType runtimeType) {
        var allFeatures = new java.util.ArrayList<String>();

        if (favorMethods) {
            runtimeType.getMethods().forEach(m -> allFeatures.add(m.getName()));
            runtimeType.getStaticMethods().forEach(m -> allFeatures.add(m.getName()));
            runtimeType.getProperties().forEach(p -> allFeatures.add(p.getName()));
            runtimeType.getStaticProperties().forEach(p -> allFeatures.add(p.getName()));
        } else {
            runtimeType.getProperties().forEach(p -> allFeatures.add(p.getName()));
            runtimeType.getStaticProperties().forEach(p -> allFeatures.add(p.getName()));
            runtimeType.getMethods().forEach(m -> allFeatures.add(m.getName()));
            runtimeType.getStaticMethods().forEach(m -> allFeatures.add(m.getName()));
        }

        String targetProperty = getProperty().toLowerCase();
        String closest = allFeatures.get(0);
        int minDistance = Text.levenshteinDistance(targetProperty, closest.toLowerCase());

        for (int i = 1; i < allFeatures.size(); i++) {
            String feature = allFeatures.get(i);
            int distance = Text.levenshteinDistance(targetProperty, feature.toLowerCase());
            if (distance < minDistance) {
                minDistance = distance;
                closest = feature;
            }
        }

        if (minDistance <= 2) {
            return " - Did you mean '" + closest + "'?";
        }
        return "";
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
