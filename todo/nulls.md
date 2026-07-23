# Custom Nulls

```java
class NotchNull {
    Span location;
    
    NotchNull(Span span) {
        this.location = span;
    }
}
```

```notch

function getTile(idx)
    return tiles[idx * height + width] recover null # this is line 4
end

tiles = []
for i in range(5)
  tiles.add(getTile(i))
end

for tile in tiles
    tile2 = tile! # if tile was null, it should error and print that it came from line 4
    
end
```