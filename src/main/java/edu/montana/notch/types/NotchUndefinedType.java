package edu.montana.notch.types;

import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;

import java.util.List;
import java.util.Set;

public final class NotchUndefinedType implements NotchType {
    public static final NotchUndefinedType INST = new NotchUndefinedType();

    private NotchUndefinedType() {}

    @Override
    public NotchMethod getMethod(String propName) {
        return null;
    }

    @Override
    public NotchFunction getStaticMethod(String propName) {
        return null;
    }

    @Override
    public NotchProperty getProperty(String propName) {
        return null;
    }

    @Override
    public NotchProperty getStaticProperty(String propName) {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "notch:undefined";
    }

    @Override
    public String getSimpleName() {
        return "undefined";
    }

    @Override
    public String getNamespace() {
        return "notch";
    }

    @Override
    public List<NotchMethod> getMethods() {
        return List.of();
    }

    @Override
    public List<NotchFunction> getStaticMethods() {
        return List.of();
    }

    @Override
    public List<NotchProperty> getProperties() {
        return List.of();
    }

    @Override
    public List<NotchProperty> getStaticProperties() {
        return List.of();
    }

    @Override
    public Class getBackingClass() {
        return null;
    }

    @Override
    public List<NotchType> getDirectSupertypes() {
        return List.of();
    }

    @Override
    public Set<NotchType> getAllSupertypes() {
        return Set.of();
    }


    @Override
    public Object newInstance(NotchRuntime runtime, Span callsite, List<Object> args, List<Span> argSpan) {
        final var diag = new Diagnostic();
        diag.setTitle("cannot construct undefined");
        diag.highlight(callsite);
        throw new NotchRuntimeException(runtime.currentStackTrace(), diag);
    }

    @Override
    public NotchField getField(String name) {
        return null;
    }
}
