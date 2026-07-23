---
title: "Functions & Closures"
subtitle: "Named functions and inline functions as first-class values"
description: "Declare named functions in Notch and write closures as first-class values, with block bodies and JVM functional-interface interop."
order: 5
---

Notch has named function declarations and closures - anonymous function values
you can pass around and call.

## Quick reference

```notch
function add(a, b)
  return a + b
end

\-> 1                # zero-arg closure
\ s -> s.length      # one arg
\ x, y -> x == y     # multiple args
```

## Named functions

Declare a function with `function`, a parameter list, and a block ended by `end`.
Use `return` to produce a value:

```notch
function add(a, b)
  return a + b
end
```

## Closures

A closure is an anonymous function value. The syntax is a backslash, an optional
parameter list, an arrow, and an expression body.

Zero arguments:

```notch
\-> 1
```

One argument:

```notch
\ s -> s.length
```

Multiple arguments:

```notch
\ x, y -> x == y
```

## Block-body closures

The arrow can also be followed by a brace-delimited statement block instead of a
single expression:

```notch
\ x -> {
  y = x + 1
  print(y)
  return y
}
```

The block runs its statements top to bottom. Use `return` to produce a value;
without one the block runs purely for side effects and the closure yields
`<undefined>`. A bare trailing expression is not a statement - use `return`, or an
expression body like `\ x -> x + 1`.

## Closures as arguments

Closures can be passed to methods that expect functions:

```notch
['a', 'ab', 'abc'].map(\ s -> s.length)
```

Returns `[1, 2, 3]`.

## Closures in maps

Map values can be closures:

```notch
x = {foo = \-> "bar"}
print(x.foo())
print(x[:foo]())
```

Both print `bar`.

## Interop with java.util.function

Notch closures convert to JVM functional interfaces:

```notch
(\ x, y -> x == y).toBiFunction()
(\ x, y -> x == y).toBiPredicate()
```

The returned objects implement `BiFunction` and `BiPredicate` respectively.

## See also

- [Java Interop](../java-interop/) for calling JVM methods that take functional interfaces.
- [Classes & Objects](../classes/) for methods bound to objects.
