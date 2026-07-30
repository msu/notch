---
title: "Errors & Exceptions"
subtitle: "Throwing, catching, and handling failures"
description: "Raise and handle errors in Notch with throw and try and catch, and how exception types resolve without an import."
order: 7
---

Notch raises failures with `throw` and handles them with `try` / `catch`.

## Quick reference

```notch
throw 'boom'

try
  throw 'boom'
catch RuntimeException as e
  print(e)
end
```

## Throwing

`throw` raises a value as an error:

```notch
throw 'boom'
```

## Catching

A `try` block runs its statements; a matching `catch` handles a thrown exception,
binding it to a name with `as`:

```notch
try
  throw 'boom'
catch RuntimeException as e
  print(e)
end
```

## Exception types

Exception types resolve by their short name in `throw`, `catch`, and `new` without
an `import`:

```notch
throw IOException("disk gone")
```

Using an exception type as a plain value elsewhere still needs an import - see
[Java Interop](../java-interop/#bringing-a-type-into-scope).

## See also

- [Error Index](/errors/) for every diagnostic code Notch reports, with a cause and a fix
  for each. This page covers raising and handling errors in your own code; the index
  covers the errors Notch itself reports.
- [Java Interop](../java-interop/) for how JVM types resolve, and for `null` handling.
- [Control Flow](../control-flow/) for the surrounding statement forms.
