package edu.montana.notch.templates.loader;

import edu.montana.notch.chisel.Source;
import edu.montana.notch.util.BetterMap;

import java.util.Map;

import static edu.montana.notch.util.Text.repr;

public class InMemoryNotchTemplateLoader extends NotchTemplateLoader {
    final BetterMap<String, Source> templates = new BetterMap<>();

    public InMemoryNotchTemplateLoader() {
    }

    public InMemoryNotchTemplateLoader(Map<String, Source> templates) {
        this.templates.putAll(templates);
    }

    public Source addTemplate(String name, String content) {
        final var source = new Source(name, content);
        templates.put(name, source);
        return source;
    }


    @Override
    public Source loadSource(String path) {
        if (!templates.containsKey(path)) {
            throw new RuntimeException("no such template %s".formatted(repr(path)));
        }
        return templates.get(path);
    }
}
