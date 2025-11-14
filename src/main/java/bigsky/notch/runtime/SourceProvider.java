package bigsky.notch.runtime;

import bigsky.utils.chisel.Span;

import java.util.List;

public interface SourceProvider {
    List<String> provideLines(String fileId, int startLine, int endLine);
    String provide(String fileId, Span span);
}
