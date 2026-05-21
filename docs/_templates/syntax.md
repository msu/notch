---
title: "Template Syntax"
subtitle: "Three content types: text, expressions, commands"
order: 3
---

Every byte of a template falls into one of three categories.

## Literal text

Anything not matched by an expression or command marker is literal text. It is written to the output verbatim, including whitespace and newlines.

```plaintext
Hello, world!
```

renders as `Hello, world!`.

## Expressions

The marker `${ ... }` evaluates the contents as a Notch expression and substitutes the result into the output. The bracketed content goes through the full Notch parser, so any Notch expression form is allowed.

```plaintext
2 + 2 = ${ 2 + 2 }
```

renders as `2 + 2 = 4`.

Expressions can reference bindings the caller passes to `render`:

```plaintext
Hello, ${ name }!
```

with `{name = "world"}` renders as `Hello, world!`.

## Commands

A line that starts with `#` is a template command. Commands drive control flow, layout, variable binding, and other directives.

```plaintext
# for item in items
  - ${ item }
# end
```

with `{items = ["a", "b", "c"]}` renders as:

```plaintext
  - a
  - b
  - c
```

See [Built-in Commands](../commands/) for the full list.

## See also

- [Built-in Commands](../commands/) for the 16 commands shipped with the engine.
- [Custom Commands](../extending/) for adding your own.
