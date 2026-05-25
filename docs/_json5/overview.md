---
title: "Overview"
subtitle: "JSON5 parser and query language"
order: 1
---

[JSON5](https://json5.org) is a superset of JSON that allows comments, unquoted keys, single-quoted strings, trailing commas, and a few other ergonomic relaxations. Notch ships a JSON5 parser and a small query language for navigating the parsed tree, both in the package `edu.montana.notch.json5`.

## What ships

- **`JSON5Parser`** - parse a JSON5 source string into a tree of `JSON5Value`.
- **`JSON5Value` hierarchy** - a sealed type tree with concrete `JSON5Object`, `JSON5Array`, `JSON5String`, `JSON5Number` (split into `JSON5Integer` and `JSON5Decimal`), `JSON5Boolean`, and `JSON5Null` cases.
- **`QueryParser` and `QueryEngine`** - a tiny path expression language for pulling values out of a parsed tree.

## Why JSON5

Plain JSON is strict by design. JSON5 is more pleasant to author and read by hand:

```notch
{
  // comments are allowed
  name: 'notch',
  versions: [
    1, 2, 3,         // trailing commas are fine
  ],
  unquoted: true,
}
```

Notch uses JSON5 anywhere it loads structured data that humans might write directly: configuration, fixtures, examples.

## See also

- [Parsing JSON5](../parsing/) for the parser API.
- [Value Types](../values/) for the value hierarchy.
- [Query Language](../queries/) for path expressions.
