# Notch Integrations with Java

Notch comes with some built in integrations with types in the JVM.

```notch
java.lang.System.out.println("hello!")
```

## Method Overloading, Method Estimation, & Coercions
One of Notch's most powerful features is the agnosticism against numerical types. We internally 
support all java native numeric types as well as `BigDecimal` and `BigInteger`. Generally,
if you pass an `int` to `void myMethod(long)` you would throw an exception because the JVM
does not do this implicit conversion at runtime. 

To fix this issue and provide a much nicer developer experience Notch allows language
designers to define a set of "coercions" that can be used when doing method estimation
and method invocation. They essentially let the runtime convert this types easily
and in an extensible way.

|   |   |   |
|---|---|---|
|   |   |   |
|   |   |   |
