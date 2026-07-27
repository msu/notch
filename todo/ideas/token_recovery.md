# Chisel Tokenizer Recovery

I think it would be good if the tokenizer could recover from token errors.

There are a couple of issues that I can think of:
- unterminated string analysis? could we mitigate with newlines?/matching pairs?

We can add in error-tokens and have some type & location data involved in it. We could also potentially add something to
the TokenType interface to be able to recover themselves...
