package edu.montana.notch.templates.loader;

import edu.montana.notch.chisel.Source;

public abstract class NotchTemplateLoader {
    public abstract Source loadSource(String path);

    /**
     * Whether this loader has a template at {@code path}. Used by {@link DefaultNotchTemplateLoader}
     * to pick a location without treating every load failure as "not found".
     */
    public boolean exists(String path) {
        try {
            loadSource(path);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
