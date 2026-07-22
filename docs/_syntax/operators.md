---
title: "Operators"
subtitle: "Precedence ladder from primary expressions to conditionals"
order: 2
---

Notch parses expressions in a precedence ladder. The lower the number, the tighter the binding.

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

## Custom operators

Notch has two operators that are not built from a single token.

### Fallback

```notch
expr ?: expr
```

The fallback operator evaluates the left-hand side; if the result is null or undefined, it evaluates the right-hand side.

### Conditional

```notch
expr if expr [else expr]
```

The conditional expression evaluates the second expression as a predicate. If it is true, the first expression is returned. If it is false and an `else` branch is present, the third expression is returned. If no `else` branch is given and the predicate is false, the result is undefined.

### Emptiness

```notch
expr is empty
expr is not empty
```

Tests whether a value is empty. Empty means: an empty string, an empty list, an empty map, or `null`. Each of these evaluates to `true`:

```notch
"" is empty
[1] is not empty
null is empty
```

## See also

- [Statements](../statements/) for the block form of `if` / `else`.
