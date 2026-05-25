---
title: "Overview"
subtitle: "Primary expressions: literals, identifiers, parens, lists, maps"
order: 1
---

Notch source is built out of expressions. The smallest expressions are called primary expressions: integer literals, boolean literals, string literals, `null`, identifiers, parenthesized expressions, list literals, and map literals.

## Literals

Integer literal:

```notch
42
```

Integer literals also accept hexadecimal, binary, and octal prefixes:

```notch
0xff      # 255
0b101     # 5
0o77      # 63
```

Boolean literals:

```notch
true
false
```

Null literal:

```notch
null
```

String literal (single or double quoted):

```notch
'hello'
"hello"
```

## Comments

Lines beginning with `//` are comments and ignored by the parser:

```notch
// this is a comment
x = 1   // trailing comments work too
```

## List literal

```notch
[1, 2, 3]
```

A trailing comma is allowed:

```notch
[1, 2, 3,]
```

## Map literal

Map keys may be bare identifiers, double-quoted strings, or terse strings prefixed with `:`.

```notch
{foo = 1, bar = 2}
{"foo foo" = 1, bar = 2}
{:foo123 = 1, bar = 2}
```

Map values can be any expression, including closures:

```notch
{foo = \-> "bar"}
```

## Identifiers and parens

Identifiers refer to bindings in the current scope. Parentheses group expressions:

```notch
(1 + 2) * 3
```

## See also

- [Operators](../operators/) for how primary expressions combine.
- [Statements](../statements/) for `if`, `for`, and assignment.
- [Closures](../closures/) for the `\-> expr` form used above.
