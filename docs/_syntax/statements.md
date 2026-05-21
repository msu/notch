---
title: "Statements"
subtitle: "if, for, blocks, then, otherwise"
order: 3
---

Notch statements are evaluated for their side effects. The two main statement forms are `if` and `for`.

## if statement

```plaintext
if true print('foo') else print('bar') end
```

The `else` branch is optional:

```plaintext
if true print('foo') end
```

## for statement

`for` iterates over any iterable: lists, strings (per-character), maps.

```plaintext
for x in 'foo' print(x) end
```

Prints `f`, `o`, `o` on separate lines.

The optional `index` clause binds the iteration index:

```plaintext
for x in 'foo' index i print(i) print(x) end
```

Outside the loop, the loop variable is `<undefined>`:

```plaintext
for x in 'foo' index i print(i) print(x) end
print(x)
```

## Block forms

A block is a sequence of statements terminated by `end`. The `if` and `for` forms above use blocks.

## then and otherwise

The single-expression variants are:

```plaintext
stmt-then    := block | 'then' expr
stmt-fallback := 'else' block 'end' | 'otherwise' expr
```

These are planned for parser implementation; the block form is shipped today.

## See also

- [Operators](../operators/) for the expression form of conditionals (`when`).
- [Closures](../closures/) for inline functions.
