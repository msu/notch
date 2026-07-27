# Indexed Errors

Convert NotchParseError to have indexed errors.

Let's have categories for parse-errors (PXXXX), tokenization errors (TXXXX), and logic errors (EXXXX).
- XXXX is a base-10 digit code
- We need an API for extensions to register new errors (use a namespace for the extension?)
  - myextension:E0001

