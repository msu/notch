package bigsky.notch.templates.runtime;

import bigsky.notch.expressions.NotchExpression;
import bigsky.notch.runtime.NotchRuntime;
import bigsky.notch.runtime.NotchRuntimeException;
import bigsky.notch.templates.NotchTemplateCommand;
import bigsky.notch.templates.NotchTemplateRegistry;
import bigsky.notch.util.Exceptions;
import org.unbescape.html.HtmlEscape;

import java.util.LinkedHashMap;
import java.util.Map;

public class NotchTemplateRuntime extends NotchRuntime {
    private LinkedHashMap<String, Object> config = new LinkedHashMap<>();
    private final NotchTemplateRegistry templates;
    private Helper helper = null;

    public NotchTemplateRuntime(
            String fileId,
            NotchTemplateRegistry templates,
            Map<String, Object> entry
    ) {
        super(fileId, entry);
        this.templates = templates;
    }

    public NotchTemplateRuntime(
            String fileId,
            NotchTemplateRuntime parent
    ) {
        super(fileId, parent);
        this.templates = parent.templates;
        this.config = parent.config;
        this.helper = parent.helper;
    }

    @Override
    public Object getSymbol(String sym) {
        var value = super.getSymbol(sym);
        if (!isUndefined(value)) {
            return value;
        }

        if (helper != null) {
            return helper.resolveSymbol(sym);
        }

        return UNDEFINED;
    }

    public void setHelper(Object newHelper) {
        if (newHelper == null) {
            helper = null;
        } else {
            helper = new Helper(newHelper);
        }
    }

    public NotchTemplateRegistry templates() {
        return templates;
    }

    public <T> T storage(StorageKey<T> key) {
        return (T) config.get(key.name);
    }

    public <T> void storage(StorageKey<T> key, T value) {
        if (value == null) {
            config.remove(key.name);
        } else {
            config.put(key.name, value);
        }
    }

    public void storage(String name, Object value) {
        if (value == null) {
            config.remove(name);
        } else {
            config.put(name, value);
        }
    }

    public String escapeText(String text) {
        return HtmlEscape.escapeHtml5(text);
    }

    public void render(NotchTemplateCommand cmd, StringBuilder out) {
        try {
            cmd.render(this, out);
        } catch (Exception e) {
            throw Exceptions.rethrow(e);
        }
    }

    public void render(NotchExpression expression, StringBuilder out) {
        try (var ignoredTrace = trace(expression.fileId, expression.span(), "<expression>")) {
            var value = evaluate(expression);
            if (!isUndefined(value)) {
                String text;
                if(value instanceof RawString ss) {
                    text = ss.rawString();
                } else {
                    String stringValue = value == null ? "" : value.toString();
                    text = escapeText(stringValue);
                }
                out.append(text);
            }
        } catch (RenderException e) {
            throw Exceptions.rethrow(e);
        } catch (NotchRuntimeException e) {
            throw new RenderException(expression.span(), e);
        }
    }

    private static class Helper {
        private final Object helper;

        Helper(Object helper) {
            this.helper = helper;
        }

        private Map<String, Object> cachedSyms = null;

        private Map<String, Object> getSymbols() {
            if (cachedSyms == null) {
                cachedSyms = new LinkedHashMap<>();
            }
            return cachedSyms;
        }

        Object resolveSymbol(String name) {
            if (helper instanceof NotchTemplateHelper h) {
                return h.resolveSymbol(name);
            }

            for (var field : helper.getClass().getFields()) {
                if (field.getName().equals(name)) {
                    try {
                        return field.get(this.helper);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return UNDEFINED;
        }
    }

    public record StorageKey<T>(Class<T> clazz, String name) {}
}
