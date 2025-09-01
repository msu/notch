package bigsky.notch.types;

public interface NotchProperty {
    String getName();
    String getAlternateName();
    Object get(Object root);
    void set(Object owner, Object val);
    boolean isStatic();
    NotchType getType();

}
