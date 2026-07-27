# Exception Micro Handling

```notch
function readFile(name)
    if not exists(name) throw IOException("file not exists") end
    return read(name)
end

repeat file in files
    contents = readFile(file) except continue  # if this fails, continue with the next iteration
end

prev = 0
repeat x in myList
    item = doSomething(prev) except break
    prev = item
end

function parseThing(file)
    contents = readFile(file) except return
    tokens = tokenize(contents) except throw TokenizeException(exception)  # implicit exception variable
    ast = parse(tokens) except throw ParseException(exception)  # implicit again
end

# essentially
<expression> "except" ('throw Exception(exception)' or 'return <value?>' or 'break' or 'continue')

# except must come before recover
<expression> "except" <ExceptionType> (throw or return or ...)
             "except" <ExceptionType2> (...)
             "recover" <ExceptionType3> (...)
             "recover" <default>
```