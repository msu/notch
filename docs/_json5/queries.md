---
title: "Query Language"
subtitle: "Path expressions over a JSON5Value"
order: 4
---

The `json5/query` subpackage provides a tiny path expression language for pulling values out of a parsed `JSON5Value` tree. A query is parsed by `QueryParser` into a `QueryExpression`, then evaluated against a value by `QueryEngine`.

## Syntax

A query is a chain of accessors starting from the root:

- `.` - the root value (the parsed tree itself).
- `.field` - field access on a `JSON5Object`.
- `[index]` - element access on a `JSON5Array` (integer index) or `JSON5Object` (string or number index).

Combine accessors to drill in:

```notch
.user.address[0].zip
```

reads `value.user.address[0].zip`.

## Expression types

Each accessor compiles to one of these `QueryExpression` nodes:

- **`RootExpression`** - the leading `.`. Returns the value the query is run against.
- **`FieldAccessExpression`** - the `.name` form. Returns the named field of an object, or null if absent.
- **`IndexExpression`** - the `[expr]` form. The inner expression is one of:
  - **`NumberExpression`** - integer literal, used for array indexing.
  - **`StringExpression`** - quoted string, used for object key access.

## Running a query

```notch
query = QueryParser.parseExpression(".user.name")
result = QueryEngine.eval(query, parsedValue)
```

`result` is itself a `JSON5Value`. To pull out a JVM-native string or number, downcast to the concrete type (`JSON5String`, `JSON5Integer`, etc.) and read its payload.

## See also

- [Value Types](../values/) for what queries return.
- [Parsing JSON5](../parsing/) for producing the tree to query.
