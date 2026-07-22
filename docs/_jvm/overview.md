---
title: "Overview"
subtitle: "Calling JVM methods, accessing properties, resolving types"
order: 1
---

Notch is designed to live inside the Java ecosystem. JVM classes, methods, and properties are first-class values in Notch code.

## Calling methods

```notch
java.lang.System.out.println("hello!")
```

When more than one overload of a method exists, Notch picks the best match by parameter count, then by direct argument-type assignability, then by whether a registered coercion can bridge the argument type to the parameter type. Less-specific matches lose to more-specific ones.

## Constructors

Call a constructor by invoking the type value:

```notch
list = java.util.ArrayList()
list.add("a")
list.add("b")
list.size
```

After the two `add` calls, `list.size` is `2`.

Constructor overload resolution uses the same `getBestMatch` logic as method invocation.

## Null handling

A property access or method call on `null` produces a diagnostic error message that points at the failing expression, rather than throwing a `NullPointerException` from inside the runtime. Use [`is empty`](../../syntax/operators/#emptiness) or compare against `null` explicitly to guard.

## Property access

Property access works on JVM objects via their getter methods. Both bean-style and direct field names are accepted.

```notch
list = [1, 2, 3]
list.size
list.size()
```

Both forms return `3`; `list.size` reads the getter, `list.size()` calls the method.

Snake-case and camelCase access both resolve to the same getter:

```notch
d = java.time.LocalDate.now()
d.dayOfMonth
d.day_of_month
```

## Type resolution

Fully-qualified class names resolve to type values:

```notch
java.lang.String
```

Returns a `NotchType` describing the class.

JVM primitives are accessible as bare identifiers:

```notch
int
```

## Bringing a type into scope

A fully-qualified name always works: `java.io.IOException`. To use a Java type by its
**short name** as a value — for example `err = IOException("disk gone")` — import it first:

```notch
import java.io.IOException
err = IOException("disk gone")
```

Exception types are the one exception to needing an import: they resolve unqualified in
`throw`, `catch`, and `new` without one (`throw IOException("...")`, `catch IOException`,
`new IOException("...")`). Using the bare name as a plain value still needs the import.

## Static methods

```notch
java.util.List.of(1, 2, 3)
```

## Static properties

```notch
java.lang.Character.TYPE
```

## Iterating an enum

Java enums iterate via the `values` static accessor:

```notch
for v in java.time.DayOfWeek.values
  print(v)
end
```

The runtime special-cases enums in its iterable coercion, so `values` works without an explicit call.

## See also

- [Coercions](../coercions/) for how Notch lets `int` flow into `long`-typed parameters.
- [Closures](../../syntax/closures/) for converting Notch closures into Java functional interfaces.
