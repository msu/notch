---
title: "Type Coercions"
subtitle: "Numeric type agnosticism and method estimation"
description: "How Notch stays agnostic about numeric types, using coercions to bridge argument and parameter types during method invocation."
order: 9
---

One of Notch's most powerful features is agnosticism about numeric types.
Internally it supports every JVM native numeric type plus `BigDecimal` and
`BigInteger`.

## The problem

The JVM does not auto-coerce numeric types at runtime. If you pass an `int` to a
method declared `void myMethod(long)`, you would normally get an exception.

## The Notch solution

Notch lets language designers define a set of coercions used during method
estimation and invocation. Coercions live in
`edu.montana.notch.types.coercions.Coercion`.

The runtime walks the registered coercions in priority order. When a candidate
method's parameter type does not match the argument type, the runtime checks
whether any coercion can bridge the two. If one can, the coercion is applied and
the method is invoked.

## Built-in coercions

The runtime ships two coercions today, both registered in
`Coercion.registerDefaultCoercions()`:

- **`BoxedToPrimitive`** - bridges the boxed/primitive pairs `Integer` and `int`,
  `Long` and `long`, `Boolean` and `boolean`, `Double` and `double`.
- **`BigDecimalToInt`** - bridges `BigDecimal` and `int`.

Numeric widening across distinct types (for example `int -> long`, `int ->
double`) is on the roadmap but does not yet ship as a registered coercion.

## Adding a coercion

The `Coercion` class is intended for extension. Today there is no public
registration API - all built-in coercions are added inside the static
`registerDefaultCoercions()` initializer. To add your own you would extend
`Coercion` and register it from inside the runtime.

## See also

- [Java Interop](../java-interop/) for the basics of JVM method calls.
