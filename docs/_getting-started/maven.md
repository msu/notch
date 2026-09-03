---
title: "Add to your project (Maven)"
subtitle: "Embed Notch as a library on the JVM"
description: "Embed Notch as a library on the JVM to run user-supplied Notch scripts from a Java app or Minecraft plugin, added via Maven Central."
order: 3
---

This page is for **embedding Notch as a library** inside your own program. If you only want to write `.notch` files and run them yourself use the [command-line](../install/) instead.

## Add the dependency

Notch is published to **Maven Central**. Use whichever build tool your project
already uses. The published version may be newer than `0.1.2`. Best to use the latest stable release.

### Maven

```xml
<dependency>
  <groupId>edu.montana.cs.notch</groupId>
  <artifactId>notch</artifactId>
  <version>0.1.2</version>
</dependency>
```

### Gradle

Make sure `mavenCentral()` is in your repositories.

```groovy
// build.gradle (Groovy DSL)
repositories { mavenCentral() }

dependencies {
  implementation 'edu.montana.cs.notch:notch:0.1.2'
}
```

```kotlin
// build.gradle.kts (Kotlin DSL)
repositories { mavenCentral() }

dependencies {
  implementation("edu.montana.cs.notch:notch:0.1.2")
}
```

## Run Notch from Java

Whether you need **a value** or **a program** will determine how you run the script.

### A value: `Notch.eval` and `Notch.render`

* `Notch.eval` runs a single expression and hands the result back as a Java object.
* `Notch.render` runs a template and returns a `String`.

```java
import edu.montana.notch.Notch;

Object v = Notch.eval("1 + 2");
int    n = Notch.eval("1 + 2", Integer.class);
String s = Notch.render("greeting", "name", "Ada");
```

- `Notch.eval(String)` evaluates one expression and returns the result.
- `Notch.eval(String, Class<T>)` coerces the result to the requested type, or
  throws if no coercion applies.
- `Notch.render(path, key, value, ...)` renders a template with key/value
  bindings.

### A whole program: `Notch.run`

When the script is a multiply statement program (assignments, loops, `print` side
effects) rather than a single expression, use `Notch.run`. It tokenizes, parses,
and executes the program against a fresh runtime.

```java
import edu.montana.notch.Notch;

Notch.run("x = 1\nprint(x + 1)");
```

The program is just source text, so it can come from anywhere. Read it from a
`.notch` file on disk rather than inlining it:

```java
import edu.montana.notch.Notch;
import java.nio.file.Files;
import java.nio.file.Path;

Notch.run(Files.readString(Path.of("app.notch")));
```

`Notch.run` throws if the program fails to parse.

Rule of thumb:

* **`eval` when you want a value back.**
* **`run` when you want to run a program.**

## Keeping the version current

The `0.1.2` above tracks `pom.xml`; bump it to match the release you depend on. The pom is the source of truth for the current version.

## See also

- [Install](../install/) for the command line runtime.
- [Java Interop](../../language/java-interop/) for how Notch calls into the JVM.

