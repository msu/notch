package bigsky.notch.types;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TypeSystemTest {

    @Test
    public void testBasicTypeLoads() {
        NotchType type = TypeSystem.getType(DemoType.class);
        assertNotNull(type);
    }

    @Test
    public void testPublicMethodsAreAvailable() {
        NotchType type = TypeSystem.getType(DemoType.class);
        NotchMethod method = type.getMethod("publicMethod");
        assertNotNull(method);
    }

    @Test
    public void testProtectedMethodsAreNotAvailable() {
        NotchType type = TypeSystem.getType(DemoType.class);
        NotchMethod method = type.getMethod("protectedMethod");
        assertNull(method);
    }

    @Test
    public void testPrivateMethodsAreNotAvailable() {
        NotchType type = TypeSystem.getType(DemoType.class);
        NotchMethod method = type.getMethod("privateMethod");
        assertNull(method);
    }

    @Test
    public void testStaticMethodsAreNotAvailable() {
        NotchType type = TypeSystem.getType(DemoType.class);
        NotchMethod method = type.getMethod("publicStaticMethod");
        assertNull(method);
    }

    @Test
    public void testStaticMethodsAreAvailableViaStaticMethod() {
        NotchType type = TypeSystem.getType(DemoType.class);
        NotchMethod method = type.getStaticMethod("publicStaticMethod");
        assertNotNull(method);
    }



}
