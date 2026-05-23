package edu.montana.notch.templates.runtime;

import edu.montana.notch.chisel.Source;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplates;
import edu.montana.notch.util.Exceptions;

import java.util.LinkedHashMap;
import java.util.Map;

public class NotchTemplateRuntime extends NotchRuntime {
    private LinkedHashMap<String, Object> config = new LinkedHashMap<>();
    private final NotchTemplates templates;
    private Helper helper = null;

    public NotchTemplateRuntime(
            Source source,
            NotchTemplates templates,
            Map<String, Object> entry
    ) {
        super(source, entry);
        this.templates = templates;
    }

    public NotchTemplateRuntime(
            Source source,
            NotchTemplateRuntime parent
    ) {
        super(source, parent);
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

    public NotchTemplates templates() {
        return templates;
    }

    public String escapeText(String text) {
        if (text == null || text.isEmpty()) return text;
        StringBuilder out = null;
        for (int i = 0, n = text.length(); i < n; i++) {
            char c = text.charAt(i);
            String replacement = switch (c) {
                case '&' -> "&amp;";
                case '<' -> "&lt;";
                case '>' -> "&gt;";
                case '"' -> "&quot;";
                case '\'' -> "&apos;";
                default -> null;
            };
            if (replacement != null) {
                if (out == null) {
                    out = new StringBuilder(text.length() + 16);
                    out.append(text, 0, i);
                }
                out.append(replacement);
            } else if (out != null) {
                out.append(c);
            }
        }
        return out == null ? text : out.toString();
    }

    public void render(NotchTemplateCommand cmd, StringBuilder out) {
        cmd.render(this, out);
    }

    public void render(NotchExpression expression, StringBuilder out) {
        try (var ignoredTrace = trace(expression, source.id)) {
            var value = evaluate(expression);
            if (!isUndefined(value)) {
                String text;
                if (value instanceof RawString ss) {
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
            throw new RenderException(e);
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
}
