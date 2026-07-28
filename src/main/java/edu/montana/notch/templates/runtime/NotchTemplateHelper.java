package edu.montana.notch.templates.runtime;

import edu.montana.notch.runtime.NotchBoundMethod;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.types.NotchMethod;
import edu.montana.notch.types.NotchProperty;
import edu.montana.notch.types.NotchType;
import edu.montana.notch.types.TypeSystem;

import java.util.List;

public interface NotchTemplateHelper {

    default Object raw(Object content) {
        if (content == null) {
            return content;
        } else {
            return new RawString(String.valueOf(content));
        }
    }

    default Object optionsForEnum(Object enumClassOrType, Object selectedValue) {
        Class<?> enumClass;

        // Handle NotchType (from Notch expressions like model.PetType)
        if (enumClassOrType instanceof NotchType) {
            enumClass = ((NotchType) enumClassOrType).getBackingClass();
        } else if (enumClassOrType instanceof Class<?>) {
            enumClass = (Class<?>) enumClassOrType;
        } else {
            throw new IllegalArgumentException("First argument must be an enum Class or NotchType");
        }

        if (!enumClass.isEnum()) {
            throw new IllegalArgumentException("optionsForEnum requires an enum class, got: " + enumClass.getName());
        }

        StringBuilder sb = new StringBuilder();
        for (Object constant : enumClass.getEnumConstants()) {
            Enum<?> enumConstant = (Enum<?>) constant;
            String value = enumConstant.name();
            String label = humanize(value);
            boolean isSelected = enumConstant.equals(selectedValue);

            sb.append("<option value=\"").append(value).append("\"");
            if (isSelected) {
                sb.append(" selected");
            }
            sb.append(">").append(label).append("</option>\n");
        }
        return new RawString(sb.toString());
    }

    default Object optionsForEnum(Enum<?> selectedValue) {
        return optionsForEnum(selectedValue.getClass(), selectedValue);
    }

    private String humanize(String enumValue) {
        if (enumValue == null || enumValue.isEmpty()) {
            return enumValue;
        }
        // Convert UPPER_CASE or UpperCase to "Upper case"
        String spaced = enumValue.replace('_', ' ');
        String lower = spaced.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    default Object resolveSymbol(NotchRuntime runtime, String name) {
        var clazz = getClass();
        NotchType type = TypeSystem.getType(clazz);
        List<NotchProperty> properties = type.getProperties();
        for (var field : properties) {
            if (field.getName().equals(name) || field.getAlternateName().equals(name)) {
                return field.get(this);
            }
        }

        List<NotchMethod> methods = type.getMethods();
        for (var method : methods) {
            if (method.getName().equals("resolveSymbol")) {
                // TODO: test this
                continue;
            }

            if (method.getName().equals(name)) {
                return new NotchBoundMethod(runtime, this, method);
            }
        }

        return NotchTemplateRuntime.UNDEFINED;
    }
}
