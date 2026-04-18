package bigsky.notch.runtime;

import bigsky.notch.chisel.Span;

import java.util.List;

public interface SourceProvider {
    default List<String> provideLines(String fileId, int startLine, int endLine) {
        var content = provide(fileId, Span.CALLSITE);
        Integer[] i = {startLine};
        return content.lines()
                .skip(startLine - 1)
                .takeWhile(line -> i[0]++ <= endLine)
                .toList();
    }

    String provide(String fileId, Span span);
}
