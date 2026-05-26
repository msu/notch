package edu.montana.notch.chisel;

import edu.montana.notch.util.BetterList;
import edu.montana.notch.util.BetterMath;
import edu.montana.notch.util.Exceptions;
import edu.montana.notch.util.Text;

import java.util.HashMap;
import java.util.List;

public class Diagnostic {
    private String title;
    private final List<Highlight> highlights = new BetterList<>();
    private final List<String> notes = new BetterList<>();
    private final StackTraceElement[] stackTrace;

    public Diagnostic() {
        stackTrace = Thread.currentThread().getStackTrace();
    }

    public Diagnostic setTitle(String title) {
        this.title = title;
        return this;
    }

    public Diagnostic highlight(Spanned spanned) {
        highlights.add(new Highlight(spanned.span()));
        return this;
    }

    public Diagnostic note(String note) {
        notes.add(note);
        return this;
    }

    public String render() {
        var out = new StringBuilder();
        render(out);
        return out.toString();
    }

    public void render(StringBuilder sb) {
        var highlighting = new HashMap<Highlight, List<String>>();
        int maxLineNoSize = 0;
        for (var highlight : highlights) {
            var startLine = highlight.span().start().line;
            var endLine = highlight.span().end().line;

            var lines = highlight.span.source().lines(startLine, endLine);
            highlighting.put(highlight, lines);

            int lineNumberLen = ("" + highlight.span.end().line).length();
            maxLineNoSize = Math.max(maxLineNoSize, lineNumberLen);
        }

        var paddingLen = BetterMath.align(2 + maxLineNoSize, 4);
        var padding = " ".repeat(paddingLen);

        if (title != null) {
            sb
                    .append(" ".repeat(paddingLen - 4))
                    .append(" ERROR: ").append(title).append('\n');
        }

        for (var highlight : highlights) {
            var startLine = highlight.span.start().line;
            var startCol = highlight.span.start().column;
            var endLine = highlight.span.end().line;
            var endCol = highlight.span.end().column;

            sb.append(" ".repeat(paddingLen - 1)).append("--> ").append(highlight.span.sourceId()).append('\n');
            sb.append(padding).append("|\n");

            var lines = highlighting.get(highlight);
            for (int i = 0; i < lines.size(); i++) {
                var line = lines.get(i);
                var lineNo = startLine + i;

                sb.append(Text.center(paddingLen, lineNo)).append("| ").append(line).append('\n');

                if (lineNo > startLine && lineNo < endLine) {
                    sb.append(padding).append("| ").append("^".repeat(line.length())).append('\n');
                } else if (lineNo == startLine && lineNo == endLine) {
                    sb.append(padding).append("| ")
                            .append(" ".repeat(startCol - 1))
                            .append("^".repeat(endCol - startCol)).append('\n');
                } else if (lineNo == startLine) {
                    sb.append(padding).append("| ")
                            .append(" ".repeat(startCol - 1))
                            .append("^".repeat(line.length() - startCol)).append('\n');
                } else if (lineNo == endLine) {
                    sb.append(padding).append("| ").append("^".repeat(endCol - 1)).append('\n');
                }
            }
            sb.append(padding).append("|\n");
        }

        var footerPadding = " ".repeat(paddingLen - 4);
        for (var footer : notes) {
            if (footer != null) {
                var lines = footer.lines().iterator();
                if (!lines.hasNext()) continue;
                sb.append(footerPadding).append("note: ").append(lines.next()).append('\n');
                while (lines.hasNext()) {
                    sb.append(padding).append(": ").append(lines.next()).append('\n');
                }
            }
        }

        renderStackTrace(sb);
    }

    private void renderStackTrace(StringBuilder out) {
        final var localTrace = Exceptions.localizeStackTrace(stackTrace);
        out.append("stack trace:\n");
        for (final var elt : localTrace) {
            out.append("- ").append(elt).append("\n");
        }
    }

    record Highlight(Span span) {
    }

    public List<String> getNotes() {
        return List.copyOf(notes);
    }
}
