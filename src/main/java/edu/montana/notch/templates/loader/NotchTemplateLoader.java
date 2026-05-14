package edu.montana.notch.templates.loader;

import edu.montana.notch.runtime.SourceProvider;
import edu.montana.notch.chisel.Span;

public abstract class NotchTemplateLoader implements SourceProvider {
    public abstract String loadTemplate(String path);

    @Override
    public String provide(String fileId, Span span) {
        var content = loadTemplate(fileId);
        var endIdx = Math.min(content.length(), span.end().index);
        var subcontent = content.substring(span.start().index, endIdx);
        return subcontent;
    }
}
