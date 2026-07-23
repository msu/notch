---
title: "Classes & Objects"
subtitle: "Declaring classes, fields, methods, and instances"
description: "Declare classes in Notch with fields and methods, reach the instance with this, and create objects with new."
order: 6
---

A class groups fields and methods under a name. Instances are created with `new`.

## Quick reference

```notch
class Point
  field x
  function getX()
    return this.x
  end
end

p = new Point()
```

## Declaring a class

A class body holds `field` declarations and `function` methods, terminated by
`end`:

```notch
class Point
  field x
  function getX()
    return this.x
  end
end
```

- **`field x`** declares an instance field.
- A `function` inside a class is a method; it can reach the instance through
  `this`.
- **`this.x`** reads the field `x` on the current instance.

## Creating an instance

Instantiate with `new`:

```notch
p = new Point()
```

Assign fields with property assignment, and call methods with dotted access:

```notch
p.x = 42
print(p.getX())
```

## See also

- [Variables & Scope](../variables/) for property assignment (`p.x = 42`).
- [Functions & Closures](../functions/) for the `function` form used as methods.
- [Java Interop](../java-interop/) for constructing JVM objects, which also uses call syntax.
