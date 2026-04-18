package bigsky.notch.templates.loader;

import bigsky.notch.runtime.SourceProvider;
import bigsky.notch.chisel.Span;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
