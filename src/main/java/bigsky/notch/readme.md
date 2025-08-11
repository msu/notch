# Notch - A modern day scripting language

yes, I did name this after another horse, sue me

| Precedence | Operators                                           |
|------------|-----------------------------------------------------|
| expr(0)    | int, boolean, string, ident, parens, list, map, set |
| expr(10)   | call, index                                         |
| expr(20)   | negate, inverse                                     | 
| expr(30)   | multiplication, division, remainder                 | 
| expr(40)   | addition, subtraction                               | 
| expr(50)   | less, greater, lessequal, greaterequal              |
| expr(60)   | equality                                            |
| expr(70)   | logical and                                         |
| expr(80)   | logical or                                          |
| expr(90)   | fallback                                            | 
| expr(100)  | conditional                                         | 

### Custom Operators

| Name        | Syntax                                    |
|-------------|-------------------------------------------|
| fallback    | `expr(89) '?:' expr(90)`                  |
| conditional | `expr(99) 'if' expr(99) [else expr(100)]` |
