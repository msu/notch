---
title: "Overview"
subtitle: "Calling JVM methods, accessing properties, resolving types"
order: 1
---

Notch is designed to live inside the Java ecosystem. JVM classes, methods, and properties are first-class values in Notch code.

## Calling methods

```plaintext
java.lang.System.out.println("hello!")
```

When more than one overload of a method exists, Notch picks the best match by parameter count, then by direct argument-type assignability, then by whether a registered coercion can bridge the argument type to the parameter type. Less-specific matches lose to more-specific ones.

## Constructors

Call a constructor by invoking the type value:

```plaintext
list = java.util.ArrayList()
list.add("a")
list.add("b")
list.size       # 2
```

Constructor overload resolution uses the same `getBestMatch` logic as method invocation.

## Null handling

A property access or method call on `null` produces a diagnostic error message that points at the failing expression, rather than throwing a `NullPointerException` from inside the runtime. Use [`is empty`](../../syntax/operators/#emptiness) or compare against `null` explicitly to guard.

## Property access

Property access works on JVM objects via their getter methods. Both bean-style and direct field names are accepted.

```plaintext
list = [1, 2, 3]
list.size      # 3
list.size()    # 3, same thing
```

Snake-case and camelCase access both resolve to the same property:

```plaintext
java.lang.Object.displayName
java.lang.Object.display_name
```

## Type resolution

Fully-qualified class names resolve to type values:

```plaintext
java.lang.String
```

Returns a `NotchType` describing the class.

JVM primitives are accessible as bare identifiers:

```plaintext
int
```

## Static methods

```plaintext
java.util.List.of(1, 2, 3)
```

## Static properties

```plaintext
java.lang.Character.TYPE
```

## Iterating an enum

Java enums iterate via the `values` static accessor:

```plaintext
for v in java.time.DayOfWeek.values
  print(v)
end
```

The runtime special-cases enums in its iterable coercion, so `values` works without an explicit call.

## See also

- [Coercions](../coercions/) for how Notch lets `int` flow into `long`-typed parameters.
- [Closures](../../syntax/closures/) for converting Notch closures into Java functional interfaces.
