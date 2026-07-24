---
title: "Operators & Expressions"
subtitle: "The precedence ladder, from primary expressions to conditionals"
description: "The Notch operator precedence ladder: arithmetic, comparison, equality, logical, string tests, and the fallback and conditional operators."
order: 3
---

Notch parses expressions in a precedence ladder. The lower the number, the
tighter the binding.

## Quick reference

```notch
items[0].trim()              # index, call
-n    !ok    not ok          # unary
2 * 3 / 4 % 5                 # multiply, divide, remainder
1 + 2 - 4                    # add, subtract
2 < 3   2 <= 3   2 > 3       # comparison
1 == 1    1 != 2             # equality
'notch' starts with 'no'     # string tests
'notch' ends with 'ch'
'notch' contains 'otc'
'' is empty    [1] is not empty
true && true    done || failed   # logical (also: and / or)
x ?: 'default'               # fallback
'yes' if cond else 'no'      # conditional
```

## Precedence table

| Precedence | Operators                                                |
|------------|----------------------------------------------------------|
| expr(0)    | int, boolean, string, ident, parens, list, map           |
| expr(10)   | call, index                                              |
| expr(20)   | negate, inverse                                          |
| expr(30)   | multiplication, division, remainder                      |
| expr(40)   | addition, subtraction                                    |
| expr(50)   | less, greater, lessequal, greaterequal                   |
| expr(60)   | equality                                                 |
| expr(70)   | logical and                                              |
| expr(80)   | logical or                                               |
| expr(90)   | fallback                                                 |
| expr(100)  | conditional                                              |

## String tests

Three infix tests operate on strings:

```notch
'notch' starts with 'no'
'notch' ends with 'ch'
'notch' contains 'otc'
```

## Custom operators

Notch has three operators that are not built from a single token.

### Fallback

```notch
expr ?: expr
```

The fallback operator evaluates the left-hand side; if the result is `null` or
undefined, it evaluates the right-hand side.

### Conditional

```notch
expr if expr [else expr]
```

The conditional evaluates the second expression as a predicate. If it is true, the
first expression is returned. If it is false and an `else` branch is present, the
third expression is returned. With no `else` branch and a false predicate, the
result is undefined.

### Emptiness

```notch
expr is empty
expr is not empty
```

Tests whether a value is empty. Empty means an empty string, an empty list, an
empty map, or `null`. Each of these is `true`:

```notch
"" is empty
[1] is not empty
null is empty
```

## See also

- [Values & Literals](../values/) for the primary expressions at the base of the ladder.
- [Control Flow](../control-flow/) for the block form of `if` / `else`.
