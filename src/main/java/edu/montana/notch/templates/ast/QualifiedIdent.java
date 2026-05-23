package edu.montana.notch.templates.ast;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Spanned;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.util.BetterList;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class QualifiedIdent implements Spanned {
    public final Span span;
    private final List<Token> names;

    public QualifiedIdent(BetterList<Token> names) {
        if (names.isEmpty()) {
            throw new IllegalArgumentException("names cannot be empty");
        }
        this.span = names.first().span.through(names.getLast());
        this.names = new LinkedList<>(names);
    }

    public Class<?> qualifiedClass() {
        var name = qualifiedName();
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String qualifiedName() {
        var out = new StringBuilder();
        int i = 0;
        for (var name : names) {
            if (i > 0) out.append('.');
            out.append(name.str());
            i += 1;
        }
        return out.toString();
    }

    // TODO: care about whitespace
    public static QualifiedIdent parse(NotchParser parser) {
        Token name = parser.consume("ident");
        if (name == null) return null;

        var names = new BetterList<Token>();
        names.add(name);

        while (parser.take(".")) {
            name = parser.require("ident", "expected namespace item");

            names.add(name);
        }

        return new QualifiedIdent(names);
    }

    public List<Token> names() {
        return Collections.unmodifiableList(names);
    }

    public Span span() {
        return span;
    }
}
