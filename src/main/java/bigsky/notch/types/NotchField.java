package bigsky.notch.types;

public interface NotchField {
    String getName();
    Object get(Object from);
    void set(Object object, Object val);
}
