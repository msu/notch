---
title: "Overview"
subtitle: "The language-agnostic foundation Notch is built on"
order: 1
---

Chisel is Notch's internal toolkit for building tokenizers, parsers, and type coercions. The Notch language is built on top of chisel; you can use the same primitives to build your own JVM-hosted languages.

<div class="callout" markdown="1">
**Audience**: this section is for embedders and language designers. If you only want to use Notch as a scripting language, you do not need to read this. Start at [Syntax Overview](../../syntax/overview/) or [REPL Getting Started](../../repl/getting-started/).
</div>

## What chisel provides

Chisel lives in the `edu.montana.notch.chisel` package and exposes:

- **`Tokenizer`** - a generic tokenizer that takes pluggable `TokenType` instances. Notch's `Notch.TOKENIZER` is a chisel `Tokenizer` configured with Notch-specific token types.
- **`BasicParser`** - a generic parser base class operating on a `TokenStream`. `NotchParser` extends `BasicParser`.
- **Token primitives** - `Token`, `TokenStream`, `TokenType`. `Token.EOF` is the sentinel returned past the end of input, so parser loops can stop on it without bounds-checking.
- **Built-in token types** - in `chisel/type/`: `TokenTypeBoolean`, `TokenTypeBoundary`, `TokenTypeCComment`, `TokenTypeIdentifier`, `TokenTypeInteger`, `TokenTypePattern`, `TokenTypePunct`, `TokenTypeString`, `TokenTypeWhitespace`. `TokenTypePunct` is itself an enum with ~30 variants (`EQ`, `EQ2`, `PLUS`, `DOT`, `QUESTION_DOT`, etc.) covering common ASCII punctuation.
- **Source location tracking** - `Location` tracks line, column, and absolute character index. `Span` wraps a start and end `Location`. `Spanned` is the interface every parsed node implements so error messages can render `file:line:column` with a caret.
- **Errors** - `ParseException`, `TokenizeException`. Both implement `Spanned`.
- **Type coercions** - the `Coercion` abstract class lives in the Notch runtime package `edu.montana.notch.types.coercions` (not under `chisel/`). It is the extension point for letting the runtime bridge type mismatches during method invocation.

## Why chisel exists

Notch's design goal of syntax extensibility relies on a stable foundation that other languages can also build on. By keeping the parsing and type-coercion machinery separate from Notch-language-specific code, third parties can fork the Notch surface and reuse the underlying machinery.

## See also

- [Extending Chisel](../extending/) for a worked example of writing a small language on top of chisel.
- [JVM Coercions](../../jvm/coercions/) for the Notch-facing view of the coercion system.
