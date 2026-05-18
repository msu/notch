package edu.montana.notch.chisel;

public interface TokenType {
    TokenData tokenize(Tokenizer t) throws TokenizeException;
}
