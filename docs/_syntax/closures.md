---
title: "Closures"
subtitle: "Inline functions as first-class values"
order: 4
---

A closure is an anonymous function value. The syntax uses a backslash, an optional parameter list, an arrow, and an expression body.

## Zero arguments

```plaintext
\-> 1
```

## One argument

```plaintext
\ s -> s.length
```

## Multiple arguments

```plaintext
\ x, y -> x == y
```

## Block-body closures

The arrow can also be followed by a brace-delimited statement block instead of a single expression:

```plaintext
\ x -> {
  y = x + 1
  print(y)
  y
}
```

The block body runs its statements top-to-bottom. The value of the closure is the value of the last expression statement.

## Closures as arguments

Closures can be passed to methods that expect functions:

```plaintext
['a', 'ab', 'abc'].map(\ s -> s.length)
```

Returns `[1, 2, 3]`.

## Closures in maps

Map values can be closures:

```plaintext
x = {foo = \-> "bar"}
print(x.foo())
print(x[:foo]())
```

Both print `bar`.

## Interop with java.util.function

Notch closures convert to JVM functional interfaces:

```plaintext
(\ x, y -> x == y).toBiFunction()
(\ x, y -> x == y).toBiPredicate()
```

The returned objects implement `BiFunction` and `BiPredicate` respectively.

## See also

- [JVM Overview](../../jvm/overview/) for calling JVM methods that take functional interfaces.
