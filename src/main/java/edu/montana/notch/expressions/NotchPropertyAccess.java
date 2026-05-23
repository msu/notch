package edu.montana.notch.expressions;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.NotchBoundMethod;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.*;
import edu.montana.notch.util.Text;

import java.util.Map;

import static edu.montana.notch.runtime.NotchRuntime.UNDEFINED;

public class NotchPropertyAccess extends NotchExpression implements DotPathMember {
    private Token property;
    private NotchExpression root;
    private boolean favorMethods;
    private final String dotPath;

    public NotchPropertyAccess(NotchExpression root, Token property) {
        super(root.span.through(property.end()));
        this.root = addChild(root);
        this.property = property;

        String dotPath = null;
        if (root instanceof DotPathMember dpm) {
            String rootDotPath = dpm.getDotPath();
            if (rootDotPath != null) {
                dotPath = "%s.%s".formatted(rootDotPath, property.str());
            }
        }
        this.dotPath = dotPath;
    }

    @Override
    public Object evaluate(NotchRuntime runtime) {
        if (dotPath != null) {
            NotchType qualifiedType = TypeSystem.getType(dotPath);
            if (qualifiedType != null) {
                return qualifiedType;
            }
        }

        Object rootValue = runtime.evaluate(root);
        if (rootValue == null || runtime.isUndefined(rootValue)) {
            if (dotPath != null) {
                var type = TypeSystem.getType(dotPath);
                if (type != null) {
                    return type;
                }
            }

            return rootValue;
        }

        NotchType runtimeType = TypeSystem.getRuntimeType(rootValue);
        if (favorMethods) {
            NotchBoundMethod method = resolveBoundMethod(rootValue, runtimeType);
            if (method != null) {
                return method;
            }
        }

        if (rootValue instanceof NotchType notchType) {
            NotchProperty staticProperty = notchType.getStaticProperty(property.str());
            if (staticProperty != null) {
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
        if (rootValue instanceof NotchType notchType && isADotPathComponent()) {
            // TODO - move into runtime to support imports
            NotchType type = TypeSystem.getType(notchType.getDisplayName() + "$" + property.str());
            if (type != null) {
                return type;
            }
        }

//        var error = new Diagnostic();
//        error.highlight(span());
//        String hint;
//        if (rootValue instanceof NotchType notchType) {
//            hint = resolveClosestFeatureName(notchType);
//        } else {
//            hint = resolveClosestFeatureName(runtimeType);
//        }
//        error.note("no property/method named %s on %s (%s)%s".formatted(Text.repr(getProperty()),
//                Text.repr(getParentDotPath()),
//                runtimeType.getDisplayName(), hint));
//        throw new NotchRuntimeException(runtime.currentStackTrace(), error);
        return UNDEFINED;
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
        if (rootValue instanceof NotchType notchType) {
            NotchMethod staticMethod = notchType.getStaticMethod(property.str());
            if (staticMethod != null) {
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

    @Override
    public String getDotPath() {
        return dotPath;
    }

    public NotchExpression getRoot() {
        return this.root;
    }

    public void setFavorMethods(boolean favorMethods) {
        this.favorMethods = favorMethods;
    }

    public String getProperty() {
        return property.str();
    }

    public String getParentDotPath() {
        var dp = getDotPath();
        return dp.substring(0, dp.lastIndexOf(DotPathMember.DOT));
    }
}
