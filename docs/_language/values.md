---
title: "Values & Literals"
subtitle: "Primary expressions: literals, identifiers, parens, lists, maps, sets"
description: "Notch primary expressions: integer, boolean, string, and null literals, list, map, and set literals, identifiers, and grouping."
order: 1
---

Notch source is built out of expressions, and the smallest expressions are
primary expressions: integer literals, boolean literals, string literals, `null`,
identifiers, parenthesized expressions, and the collection literals.

## Quick reference

```notch
42   0xff   0b101   0o77     # integers (decimal, hex, binary, octal)
true   false                 # booleans
null                         # null
'hello'   "hello"            # strings
[1, 2, 3]                    # list
{'foo' -> 1, 'bar' -> 2}     # map
{1, 2, 3}                    # set
(1 + 2) * 3                  # grouping
```

## Numbers

An integer literal:

```notch
42
```

Integer literals also accept hexadecimal, binary, and octal prefixes. `0xff`,
`0b101`, and `0o77` are `255`, `5`, and `63`:

```notch
0xff
0b101
0o77
```

## Booleans and null

```notch
true
false
null
```

## Strings

A string literal is single- or double-quoted:

```notch
'hello'
"hello"
```

## Lists

```notch
[1, 2, 3]
```

A trailing comma is allowed:

```notch
[1, 2, 3,]
```

## Maps

A map is a brace list of `key -> value` pairs. Keys are expressions - a bare name
is read as a variable, so quote string keys (single- or double-quoted, or the
terse `:` form):

```notch
{'foo' -> 1, 'bar' -> 2}
{"foo foo" -> 1, "bar" -> 2}
{:foo123 -> 1, :bar -> 2}
```

Map values can be any expression, including closures:

```notch
{'foo' -> \-> "bar"}
```

## Sets

A brace list without `key -> value` pairs is a set:

```notch
{1, 2, 3}
```

## Identifiers and parentheses

Identifiers refer to bindings in the current scope. Parentheses group
expressions:

```notch
(1 + 2) * 3
```

## See also

- [Operators & Expressions](../operators/) for how primary expressions combine.
- [Variables & Scope](../variables/) for binding names to values.
- [Functions & Closures](../functions/) for the `\-> expr` form used above.
