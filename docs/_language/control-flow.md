---
title: "Control Flow"
subtitle: "print, if, for, and the repeat loops"
description: "Notch control flow: print, if and else, for loops with an index, and the repeat times, while, and until loops with break and continue."
order: 4
---

Statements are evaluated for their side effects. Control-flow statements decide
what runs and how often.

## Quick reference

```notch
print(x)
if cond print('foo') else print('bar') end
for x in 'foo' print(x) end
for x in 'foo' index i print(i) end
repeat 3 times print(it) end
repeat while x < 3 x = x + 1 end
repeat until x >= 3 x = x + 1 end
break
continue
```

## print

```notch
print(x)
```

## if

```notch
if true print('foo') else print('bar') end
```

The `else` branch is optional:

```notch
if true print('foo') end
```

## for

`for` iterates over any iterable: lists, strings (per character), and maps.

```notch
for x in 'foo' print(x) end
```

That prints `f`, `o`, `o` on separate lines.

The optional `index` clause binds the iteration index:

```notch
for x in 'foo' index i print(i) print(x) end
```

The loop variable does not escape the loop - see [Variables & Scope](../variables/).

## repeat

`repeat` has three forms. `repeat N times` runs a block `N` times, binding the
current count to `it`:

```notch
repeat 3 times print(it) end
```

`repeat while` runs as long as its predicate holds; `repeat until` runs until the
predicate becomes true:

```notch
repeat while x < 3 x = x + 1 end
repeat until x >= 3 x = x + 1 end
```

Inside any loop, `break` exits and `continue` skips to the next iteration:

```notch
break
continue
```

## Blocks

A block is a sequence of statements terminated by `end`. The `if`, `for`, and
`repeat` forms above all use blocks.

## See also

- [Variables & Scope](../variables/) for how loop variables are scoped.
- [Operators & Expressions](../operators/) for the expression form of conditionals.
- [Functions & Closures](../functions/) for inline functions.
