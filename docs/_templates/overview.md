---
title: "Overview"
subtitle: "Notch's built-in template engine"
order: 1
---

`NotchTemplates` is a template engine that composes Notch's tokenizer and expression parser to render text with embedded expressions and commands. Use it when you want a string-output template that can call into Notch evaluation.

A template file is made of three kinds of content:

- **Literal text** - copied to output verbatim.
- **Expressions** - the form `${ expression }` evaluates `expression` as Notch and substitutes the result.
- **Commands** - a line beginning with `#` invokes a template command (control flow, layout, macros, etc.).

The engine lives in the package `edu.montana.notch.templates`. The main entry points are:

- `NotchTemplates.TOKENIZER` - a preconfigured `Tokenizer` for template source.
- `NotchTemplateRegistry` - loads and caches templates; renders them to output.
- `NotchTemplateLoader` - the interface for plugging in a source of template files. Two implementations ship: `NotchTemplateClasspathLoader` and `NotchTemplateFilesystemLoader`.
- `NotchTemplateCommand` - the base class for template commands. Subclass and register to add your own.

## See also

- [Getting Started](../getting-started/) for a worked load-and-render example.
- [Template Syntax](../syntax/) for the markers in detail.
- [Built-in Commands](../commands/) for the 16 commands shipped out of the box.
- [Custom Commands](../extending/) for adding your own.
