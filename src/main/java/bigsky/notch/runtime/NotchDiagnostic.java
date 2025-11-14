package bigsky.notch.runtime;

import bigsky.utils.BetterList;
import bigsky.utils.BetterMath;
import bigsky.utils.Text;
import bigsky.utils.chisel.Span;

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
            var lines = sp.provideLines(highlight.fileId, highlight.span());
            highlighting.put(highlight, lines);

            int lineNumberLen = ("" + highlight.span.end().line).length();
            maxLineNoSize = Math.max(maxLineNoSize, lineNumberLen);
        }

        var paddingLen = BetterMath.align(2 + maxLineNoSize, 4);
        var padding = " ".repeat(paddingLen);

        if (title != null) {
            sb.append("= ").append(title).append('\n');
        }

        for (var highlight : highlights) {
            var startLine = highlight.span.start().line;
            var startCol = highlight.span.start().column;
            sb.append(padding).append("/ ").append(highlight.fileId).append(" [").append(startLine).append(":").append(startCol).append("]\n");

            var lines = highlighting.get(highlight);
            for (int i = 0; i < lines.size(); i++) {
                var line = lines.get(i);
                var lineNo = startLine + i;
                sb
                        .append(Text.center(paddingLen, lineNo))
                        .append("| ")
                        .append(line)
                        .append('\n');
            }
            sb.append('\n');
        }
        sb.append('\n');

        var footerPadding = " ".repeat(paddingLen - 4);
        for (var footer : notes) {
            var lines = footer.lines().iterator();
            if (!lines.hasNext()) continue;
            sb.append(footerPadding).append("note: ").append(lines.next()).append('\n');
            while (lines.hasNext()) {
                sb.append(padding).append(": ").append(lines.next()).append('\n');
            }
        }
    }

    record Highlight(String fileId, Span span) {
    }
}
