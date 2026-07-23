package edu.montana.notch.types;

import edu.montana.notch.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

import static edu.montana.notch.util.Exceptions.safelyEval;

public class NotchJavaField implements NotchField {
    private static final Logger log = LoggerFactory.getLogger(NotchJavaField.class);
    private Field field;

    public NotchJavaField(Field field) {
        this.field = field;
        try {
            field.setAccessible(true);
        } catch (SecurityException e) {
            log.error("Cannot access field {}.", field.getName(), e);
        }
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public Object get(Object from) {
        return safelyEval(() -> field.get(from));
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
