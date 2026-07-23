# Structure Command

Prints the structure of the type of a value, including *public* methods & properties & extended interfaces.

TODO: add `String structure(Object value)` as a global in the language

Format:
```
class <simple class name> (<class namespace>):
  properties:
  - <name>: <simple type name> (<type qualified name minus name>)
  methods:
  - <name>: <return type>(<arg name>: <qualified arg type>)
  interfaces:
  - <name> (<interface qualified name minus name>)
```

Notes:
- any instance of "Class" in a property should be printed as "class <displayName>"
- I say "qualified name" which is inter-changeable with "display name"

```notch
import com.google.StupidlyComplicated as SC

print(structure(StupidlyComplicated(...)))
    # class StupidlyComplicated (com.google)
    #  properties:
    #   - container: StupidContainer (com.google.ct) = (value... truncate to 100 chars)
    #   - ...
    #   - clazz: Class (java.base)  # no, special case for the 'Class<...>' type
    #   - clazz: class java.util.ArrayList (yes)  # do this instead (use displayName I think)
    #  methods:
    #   - foo: void(x: int, y: int)
    #  interfaces:
    #   - 

print(structure(3))
# class Integer (java.base)
#  methods:
#  - longValue: Long()
#  interfaces:
#  - Number (java.base)

print(structure("hello, world"))
# class String (java.base)
# ...


```