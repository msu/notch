# Undefined Escaping The Java Boundary

When a Notch expression evaluates to `<undefined>` and is passed to a Java method with an
`Object`-typed parameter, the sentinel is accepted silently and stored in host state, so the
failure surfaces later as a `ClassCastException` on `edu.montana.notch.runtime.NotchRuntime$1`
inside the embedder's own code, with no Notch source location to trace it back to.

```notch
cart.add(missing?)        # --> "added"  (no error: sentinel satisfies Object)
cart.add(cart.nosuchprop) # --> "added"
# cart.items is now [2, <undefined>, <undefined>]
# cart.total() later dies in host code:
#   class edu.montana.notch.runtime.NotchRuntime$1 cannot be cast to class java.lang.Number
```

Claude thinks the following is the cause not yet verified:
Cause: `NotchJavaMethod.distanceFromValues` scores the sentinel against `Object` by walking its
superclass chain, matching at distance 2, and `NotchMethodInvocationExpression` passes arg values
to `invoke` without converting `UNDEFINED`. Affects `Object`, varargs, and erased generics
(`List<T>`, `Map<K,V>`) common shapes in embedder APIs.

Fix: convert `UNDEFINED` to `null` where args are evaluated in `NotchMethodInvocationExpression`,
before they reach Java.