---
title: "Parsing JSON5"
subtitle: "Turning source into a JSON5Value tree"
order: 2
---

`JSON5Parser` reads a JSON5 source string and returns a `JSON5Value` tree.

## Basic parse

```notch
parser = new JSON5Parser("config.json5", source)
value  = parser.parseValue()
```

The first argument is a `fileId` used in error messages. The second is the JSON5 source. `parseValue()` returns whatever top-level value the source defines (most commonly a `JSON5Object` or `JSON5Array`, but any `JSON5Value` is legal).

## Pre-tokenized input

If you have already tokenized the source through chisel and want to share that work, `JSON5Parser` also accepts a `TokenStream`:

```notch
parser = new JSON5Parser(tokenStream)
value  = parser.parseValue()
```

This is mostly useful when JSON5 fragments are embedded inside a larger language and the outer parser has already chunked the source.

## Errors

Parse failures throw `ParseException` (from `chisel`). Catch it at the call site to report `file:line:column` with a caret pointing at the bad token.

## See also

- [Value Types](../values/) for what `parseValue()` returns.
- [Query Language](../queries/) for navigating the result.
