package edu.montana.notch.chisel;

public interface TokenType {
    Token tokenize(Tokenizer t) throws TokenizeException;
}
