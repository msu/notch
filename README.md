# Notch - Yet Another Scripting Language



## Semantics

The semantics of the language.

### Variable, Property, & Invocation Semantics

We deliberated for a long time on what we thought were the proper semantics. We want the
language to be easy to iterate on and slow to cause errors. We decided to implement
null-coalescence instead of "undefined variable/property/method on ..."

```notch
foo             # --> <UNDEFINED>
foo!            # error: no variable "foo"
foo.bar         # --> <UNDEFINED>
foo.bar!        # error: no property "bar" on <UNDEFINED>
foo.bar()       # --> <UNDFINED>
foo.bar!()      # error: no method "bar" on <UNDEFINED>
foo.bar.baz.bad # --> <UNDEFINED>
```
