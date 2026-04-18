package bigsky.notch.types;

import bigsky.notch.util.Text;

import java.lang.reflect.*;

import static bigsky.notch.util.Exceptions.rethrow;

public class NotchJavaProperty implements NotchProperty {

    public static final String GET_PREFIX = "get";
    public static final String SET_PREFIX = "set";
    public static final String IS_PREFIX = "is";

    private final Getter getter;
    private final Setter setter;

    private final boolean staticProperty;
    private final Class parentClass;
    private final String name;

    public static boolean isPropertyMethod(Method method) {
        return method.getParameterTypes().length == 0;
    }

    public static String propertyNameFor(Method method) {
        String name = method.getName();
        if (name.startsWith("get")) {
            return Text.decapitalize(name.substring(3));
        } else if (method.getName().startsWith("is")) {
            return Text.decapitalize(name.substring(2));
        } else {
            return name;
        }
    }

    @Override
    public NotchType getType() {
        return TypeSystem.getType(getter.getType());
    }

    public NotchJavaProperty(Class aClass, String propName, boolean staticProperty) {
        this.staticProperty = staticProperty;
        this.getter = resolveGetter(aClass, propName);
        this.setter = getter == null ? null : getter.resolveSetter();
        this.parentClass = aClass;
        this.name = propName;
    }

    private Getter resolveGetter(Class aClass, String propName) {
        String capitalizedName = Text.capitalize(propName);
        String[] methodPossibilities;

        // if the prop name is all lowercase, try to resolve a no prefix version of the getter last
        methodPossibilities = new String[]{GET_PREFIX + capitalizedName, IS_PREFIX + capitalizedName, propName};

        for (String methodName : methodPossibilities) {
            try {
                Method method = aClass.getMethod(methodName);
                if (matchesStaticFlag(method)) {
                    return new MethodGetter(method);
                }
            } catch (NoSuchMethodException nsme) {
                // ignore
            }
        }
        try {
            Field field = aClass.getField(propName);
            if (matchesStaticFlag(field)) {
                return new FieldAccessor(field);
            }
        } catch (NoSuchFieldException nfe){
            // ignore
        }
        return null;
    }

    private boolean matchesStaticFlag(Member method) {
        return Modifier.isStatic(method.getModifiers()) == staticProperty;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAlternateName() {
        return Text.snakeCase(getName());
    }

    @Override
    public Object get(Object root) {
        if (getter == null) {
            return null;
        }
        try {
            return getter.get(root);
        } catch (Exception e) {
            throw rethrow(e);
        }
    }

    @Override
    public void set(Object owner, Object val) {
        setter.set(owner, val);
    }

    @Override
    public boolean isStatic() {
        return staticProperty;
    }

    private interface Getter {
        Object get(Object root);
        Class getType();
        Setter resolveSetter();
        boolean isStatic();
    }

    private interface Setter {
        void set(Object root, Object value);
    }

    private class MethodGetter implements Getter {
        private final Method method;
        public MethodGetter(Method method) {
            this.method = method;
        }

        @Override
        public Object get(Object root) {
            try {
                return method.invoke(root);
            } catch (Exception e) {
                throw rethrow(e);
            }
        }

        @Override
        public Class getType() {
            return method.getReturnType();
        }

        @Override
        public Setter resolveSetter() {
            try {
                String setterName = method.getName();
                // flip get to set
                if (setterName.startsWith("get")) {
                    setterName = "set" + setterName.substring(3);
                }
                return new MethodSetter(method.getDeclaringClass().getMethod(setterName, method.getReturnType()));
            } catch (NoSuchMethodException e) {
                return null;
            }
        }

        @Override
        public boolean isStatic() {
            return Modifier.isStatic(method.getModifiers());
        }
    }

    private class FieldAccessor implements Getter, Setter {
        private final Field field;

        public FieldAccessor(Field field) {
            this.field = field;
        }

        @Override
        public Object get(Object root) {
            try {
                return field.get(root);
            } catch (IllegalAccessException e) {
                throw rethrow(e);
            }
        }

        @Override
        public Class getType() {
            return null;
        }

        @Override
        public Setter resolveSetter() {
            return this;
        }

        @Override
        public void set(Object root, Object value) {
            try {
                field.set(root, value);
            }  catch (IllegalAccessException e) {
                throw rethrow(e);
            }
        }

        @Override
        public boolean isStatic() {
            return Modifier.isStatic(field.getModifiers());
        }
    }

    private class MethodSetter implements Setter {
        private final Method method;

        public MethodSetter(Method method) {
            this.method = method;
        }

        @Override
        public void set(Object root, Object value) {
            try {
                method.invoke(root, value);
            } catch (Exception e) {
                throw rethrow(e);
            }
        }
    }

    @Override
    public String toString() {
        return getAlternateName();
    }
}
