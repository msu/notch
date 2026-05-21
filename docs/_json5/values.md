---
title: "Value Types"
subtitle: "The JSON5Value hierarchy"
order: 3
---

`JSON5Value` is the sealed abstract base of the value tree produced by `JSON5Parser`. Every parsed node is exactly one of the cases below.

## `JSON5Object`

A keyed collection. Keys are strings (parsed from either quoted strings or bare identifiers in JSON5 source). Values are any `JSON5Value`.

## `JSON5Array`

An ordered collection of `JSON5Value`. Indexable from zero.

## `JSON5String`

A textual value. JSON5 source can use single or double quotes; the parsed form is the same `JSON5String` either way.

## `JSON5Number`

The numeric base. Itself sealed and split into two concrete cases:

- **`JSON5Integer`** - whole-number literals (e.g. `42`, `0xff`).
- **`JSON5Decimal`** - literals with a fractional component or exponent (e.g. `3.14`, `1e9`).

The split lets consumers preserve integer fidelity instead of widening everything to floating point.

## `JSON5Boolean`

`true` or `false`.

## `JSON5Null`

The `null` value. Distinct from a missing key on a `JSON5Object`.

## See also

- [Parsing JSON5](../parsing/) for producing one of these from source.
- [Query Language](../queries/) for navigating a tree of them.
