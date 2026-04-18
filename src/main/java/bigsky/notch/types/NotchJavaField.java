package bigsky.notch.types;

import bigsky.notch.util.Exceptions;

import java.lang.reflect.Field;

import static bigsky.notch.util.Exceptions.safely;
import static bigsky.notch.util.Exceptions.safelyEval;

public class NotchJavaField implements NotchField {
    private Field field;

    public NotchJavaField(Field field) {
        this.field = field;
        field.setAccessible(true);
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public Object get(Object from) {
        return safelyEval(()->field.get(from));
    }

    @Override
    public void set(Object object, Object val) {
        try {
            field.set(object, val);
        } catch (Exception e) {
            throw Exceptions.rethrow(e);
        }
    }
}
