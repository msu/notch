package edu.montana.notch.chisel;

import java.util.List;

public class ParseErrors extends RuntimeException {
    public final List<Diagnostic> diagnostics;

    public ParseErrors(List<Diagnostic> diagnostics) {
        this.diagnostics = List.copyOf(diagnostics);
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        for (Diagnostic d : diagnostics) {
            if (!sb.isEmpty()) sb.append("\n");
            sb.append(d.render());
        }
        return sb.toString();
    }
}
