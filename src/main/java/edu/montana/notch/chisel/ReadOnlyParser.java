package edu.montana.notch.chisel;

public interface ReadOnlyParser {

    Token currentToken();

    Token peekNext();

    Token lastToken();

    boolean atEnd();
}