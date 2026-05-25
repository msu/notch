---
title: "Extending Chisel"
subtitle: "Writing your own language on the chisel foundation"
order: 2
---

This page sketches how to build a small language using chisel primitives. The Notch parser itself is the canonical worked example; this is an outline.

## 1. Configure a tokenizer

Compose token types into a `Tokenizer`:

```notch
Tokenizer T = new Tokenizer()
    .withTokenType(WHITESPACE)
    .withTokenType(BOOL)
    .withTokenType(MY_KEYWORD)
    .withTokenType(IDENT)
    .withTokenType(INT)
    .withTokenType(STR)
    .withTokenTypes(TokenTypePunct.common());
```

`TokenTypePunct.common()` returns the common punctuation tokens. You can add your own `TokenType` implementations by subclassing or composing.

## 2. Write a parser

Extend `BasicParser`:

```notch
public class MyParser extends BasicParser {
    public MyParser(String fileId, String src) {
        this(T.tokenize(fileId, src));
    }

    public MyExpression parseExpression() {
        // recursive descent here
    }
}
```

The base class provides:

- `location()`, `take()`, `peek()` - position-tracking primitives.
- `ignoreTypes(TokenType...)` - skip tokens of the listed types when scanning (typical use: whitespace and comments).
- `temporaryEndTokens(TokenType[], () -> T)` - run a sub-parser that treats the listed tokens as end-of-stream. Useful when parsing a list element that should stop at `,` or `]` without consuming them.

## 3. Add coercions

If your language uses JVM types, register coercions so type mismatches at method-call sites get bridged automatically. See `src/main/java/edu/montana/notch/types/coercions/Coercion.java` for the abstract base.

## 4. Render errors with spans

Throw `ParseException` from your parser. Catch at the entry point and use the `Spanned` interface to render a file:line:column caret message.

## See also

- [Overview](../overview/) for the chisel API surface.
- The Notch source itself: `src/main/java/edu/montana/notch/NotchParser.java` extends `BasicParser`; `Notch.java` configures the tokenizer.
