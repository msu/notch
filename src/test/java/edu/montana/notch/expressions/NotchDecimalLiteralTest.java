package edu.montana.notch.expressions;

import edu.montana.notch.Notch;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NotchDecimalLiteralTest {

    @Test
    public void plainDecimal() {
        assertEquals(1.5, Notch.<Object>eval("1.5"));
        assertEquals(0.25, Notch.<Object>eval("0.25"));
    }

    @Test
    public void leadingDot() {
        assertEquals(0.5, Notch.<Object>eval(".5"));
        assertEquals(1.5, Notch.<Object>eval("1 + .5"));
        assertEquals(-0.5, Notch.<Object>eval("-.5"));
    }

    @Test
    public void exponent() {
        assertEquals(1e10, Notch.<Object>eval("1e10"));
        assertEquals(1.5e-3, Notch.<Object>eval("1.5e-3"));
        assertEquals(2e4, Notch.<Object>eval("2E+4"));
    }

    @Test
    public void suffixes() {
        assertEquals(1.5f, Notch.<Object>eval("1.5f"));
        assertEquals(3f, Notch.<Object>eval("3F"));
        assertEquals(2.0, Notch.<Object>eval("2d"));
        assertEquals(1e3f, Notch.<Object>eval("1e3f"));
    }

    @Test
    public void arithmetic() {
        assertEquals(3.0, Notch.<Object>eval("1.5 * 2"));
        assertEquals(2.5, Notch.<Object>eval("5 / 2.0"));
        assertEquals(true, Notch.<Object>eval("0.1 < 0.2"));
    }

    @Test
    public void integersUnaffected() {
        assertEquals(42, Notch.<Object>eval("42"));
        assertEquals(31, Notch.<Object>eval("0x1f"));
        assertEquals(0xde, Notch.<Object>eval("0xde"));
        assertEquals(2, Notch.<Object>eval("0b10"));
    }

    @Test
    public void memberAccessOnLiteralsUnaffected() {
        assertEquals("1.5", Notch.<Object>eval("1.5.toString()"));
        assertEquals("1", Notch.<Object>eval("1.toString()"));
    }
}
