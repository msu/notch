package edu.montana.notch.types;

public interface NotchField {
    String getName();
    Object get(Object from);
    void set(Object object, Object val);
}
