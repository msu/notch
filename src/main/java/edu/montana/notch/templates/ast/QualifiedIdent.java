package edu.montana.notch.templates.ast;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Location;
import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Token;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static edu.montana.notch.chisel.type.TokenTypeIdentifier.IDENT;
import static edu.montana.notch.chisel.type.TokenTypePunct.DOT;

public class QualifiedIdent {
    public final Location start, end;
    private final List<Token> names;

    public QualifiedIdent(List<Token> names) {
        if (names.isEmpty()) {
            throw new IllegalArgumentException("names cannot be empty");
        }
        this.start = names.get(0).start;
        this.end = names.get(names.size() - 1).end;
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
        Token name = parser.consume(IDENT);
        if (name == null) return null;

        var names = new LinkedList<Token>();
        names.add(name);

        while (parser.take(DOT)) {
            name = parser.require(IDENT, "expected namespace item");

            names.add(name);
        }

        return new QualifiedIdent(names);
    }

    public List<Token> names() {
        return Collections.unmodifiableList(names);
    }

    public Span span() {
        return new Span(start, end);
    }
}
