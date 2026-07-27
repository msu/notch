# Flattening For Loops :)

```
l1 = [1, 2, 3]
l2 = [:a, :b, :c]

for a, b in l1, l2
    print(a + b)
end
# 1a, 1b, 1c, 2a, 2b, 2c, 3a, 3b, 3c

for a in l1
  for b in l2
    print(a + b)
  end
end



row = [0].repeat(length)
delay = 80.longValue()
```