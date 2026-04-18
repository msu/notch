package bigsky.notch.util;

import bigsky.notch.json5.JSON5Array;
import bigsky.notch.json5.JSON5Object;
import bigsky.notch.json5.JSON5Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSON5QueryTest {
    private TestInfo testInfo;

    @BeforeEach
    public void setup(TestInfo testInfo) {
        this.testInfo = testInfo;
    }

    private <T extends JSON5Value> T parse(String query) {
        return JSON5.parse("<test:" + testInfo.getDisplayName() + ">", query);
    }

    @Test
    public void rootValue() {
        JSON5Object json = parse("{message: 'hello, world'}");
        var value = json.query(".", JSON5Object.class);
        assertEquals(json, value);
    }

    @Test
    public void simpleProperty() {
        JSON5Object json = parse("{message: 'hello, world'}");
        var value = json.query(".message", String.class);
        assertEquals("hello, world", value);
    }

    @Test
    public void chainedProperty() {
        JSON5Object json = parse("{hello: {world: 'hello, world!'}}");
        var value = json.query(".hello.world", String.class);
        assertEquals("hello, world!", value);
    }

    @Test
    public void simpleIndex() {
        JSON5Array arr = parse("[{msg: 'ello', msg2: 'world'}]");
        var value = arr.query(".[0].msg", String.class);
        assertEquals("ello", value);
    }

    @Test
    public void chainedArrayProperty() {

    }
}
