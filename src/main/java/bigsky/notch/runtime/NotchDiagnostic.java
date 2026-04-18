package bigsky.notch.runtime;

import bigsky.notch.util.BetterList;
import bigsky.notch.util.BetterMath;
import bigsky.notch.util.Text;
import bigsky.notch.chisel.Span;

import java.util.HashMap;
import java.util.List;

public class NotchDiagnostic {
    private String title;
    private List<Highlight> highlights = new BetterList<>();
    private List<String> notes = new BetterList<>();

    public void setTitle(String title) {
        this.title = title;
    }

    public void highlight(String fileId, Span span) {
        highlights.add(new Highlight(fileId, span));
    }

    public void note(String note) {
        notes.add(note);
    }

    public String render(SourceProvider sp) {
        var out = new StringBuilder();
        render(sp, out);
        return out.toString();
    }

    public void render(SourceProvider sp, StringBuilder sb) {
        var highlighting = new HashMap<Highlight, List<String>>();
        int maxLineNoSize = 0;
        for (var highlight : highlights) {
            var startLine = highlight.span().start().line;
            var endLine = highlight.span().end().line;

            var lines = sp.provideLines(highlight.fileId, startLine, endLine);
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

            sb.append(" ".repeat(paddingLen - 1)).append("--> ").append(highlight.fileId).append('\n');
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
    }

    record Highlight(String fileId, Span span) {
    }
}
