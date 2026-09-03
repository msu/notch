---
title: "Values & Literals"
subtitle: "Primary expressions: literals, identifiers, parens, lists, maps, sets"
description: "Notch primary expressions: integer, boolean, string, and null literals, list, map, and set literals, identifiers, and grouping."
order: 1
---

Notch is built out of expressions and statements. This docs page address the expressions.

## Quick reference

```notch
42   0xff   0b101   0o77     # integers (decimal, hex, binary, octal)
true   false                 # booleans
null                         # null
'hello'   "hello"   :hello   # strings
f'{count} items'   f:{count} # interpolated strings
[1, 2, 3]   []               # list, empty list
{'foo' -> 1}   {}            # map, empty map
{1, 2, 3}   {,}              # set, empty set
count   count?               # identifier, lenient read
(1 + 2) * 3                  # grouping
```

## Integers

An integer literal:

```notch
42
```

Integers are the only numeric literal. There is no decimal or floating-point
form currently, and a literal must be in the range `0` to `2147483647`. See
[Type Coercions](../coercions/) for how Notch reaches the other JVM numeric
types.

Integer literals also accept hexadecimal, binary, and octal prefixes: `0xff`,
`0b101`, and `0o77` are `255`, `5`, and `63`.

## Booleans and null

```notch
true
false
null
```

`null` is a value you can write and pass around, the same one Java uses. Notch
also has `<undefined>`, which has no literal form. A plain read of a name that
was never bound is an error, not `<undefined>`.

## Strings

A string literal is single or double quoted.

```notch
'hello'
"hello"
```

A quoted string cannot span a line.

Inside a string, `\\`, `\n`, `\r`, and `\t` are the backslash, newline, carriage
return, and tab. The enclosing quote escapes itself  `\'` inside `'...'` and
`\"` inside `"..."`.

A `:` starts a terse string running to the next space, `,`, `)`, `]`, `}`, or end of line. A tab does not end it. It needs no closing quote:

```notch
:hello
```

A terse string is taken literally: `\n` and the other escapes are ordinary
characters inside it.

An `f` prefix is an interpolated string. Each `{expression}` inside it is
evaluated and substituted. 

Write {% raw %}`{{`{% endraw %} for a literal `{` and a `}` needs no escape.

```notch
count = 3
print(f'{count} items')
# 3 items
print(f"one more is {count + 1}")
# one more is 4
print(f:{count}items)
# 3items
```

## Lists

```notch
[1, 2, 3]
```

A trailing comma is allowed in list, map, and set literals: `[1, 2, 3,]`. The
empty list is `[]` Unlike a string, a collection literal may span lines, as long as the commas are there.

## Maps

A map is a brace literal of `key -> value` pairs, and `{}` is the empty map.
Keys are expressions thus a bare name is read as a variable.

```notch
# Variable
{foo -> 1}
# String
{'foo' -> 1, "bar" -> 2}
{:foo123 -> 1, :bar -> 2}
```

* A terse key needs a space before the `->`. A terse string runs until a space, so
  `{:foo->1}` reads as the single string `foo->1` and gives you a set, not a map.
* A repeated key keeps the last value written, silently: `{'a' -> 1, 'a' -> 2}`
  is `{a=2}`.
* A map literal does not keep the order you wrote it in iterating one can yield
  the pairs in any order.

## Sets

A brace literal without `key -> value` pairs is a set:

```notch
{1, 2, 3}
```

A set keeps the order you wrote and drops duplicates. Sets print with brackets,
so `{3, 1, 2, 1}` prints as `[3, 1, 2]` . It is still a set not a list.

The empty set is `{,}`, not `{}`. This distinguishes it from a map.

## Identifiers and parentheses

An identifier starts with a letter, `_`, or `$`, and continues with those plus
digits.

```notch
count   _internal   $ref   item2   café
```

Reserved words cannot be used as names. See [Grammar](../grammar/) for the list of reserved words.

Parentheses group an expression so it binds before the operators around it:

```notch
(1 + 2) * 3
```

That is `9`; without the parentheses `1 + 2 * 3` is `7`.

## Comments

`#` and `//` run to the end of the line. `/* */` is a block comment: it may span
lines, or sit inside an expression, so `x = 1 /* inline */ + 2` is `3`. Block
comments do not nest the first `*/` closes the one that is open and one that
is never closed is a tokenize error.

```notch
x = 1   # to end of line
y = 2   // also to end of line
z = 3   /* block */
```

* All three are ordinary characters inside a string, quoted or terse, so
  `'a # b'` is the whole string.
