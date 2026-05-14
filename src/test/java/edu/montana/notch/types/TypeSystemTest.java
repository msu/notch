package edu.montana.notch.types;

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

    @Test
    public void testPropertiesResolveProperly() {
        var type = TypeSystem.getType(DemoType.class);

        var prop = type.getProperty("prop1");
        assertNotNull(prop);

        prop = type.getProperty("prop2");
        assertNotNull(prop);

        prop = type.getProperty("prop3");
        assertNotNull(prop);

        prop = type.getProperty("propFour");
        assertNotNull(prop);
    }

    @Test
    public void testPropertiesAlsoResolveAsFunctionsProperly() {
        var type = TypeSystem.getType(DemoType.class);

        var method = type.getMethod("prop1");
        assertNotNull(method);

        method = type.getMethod("getProp2");
        assertNotNull(method);

        method = type.getMethod("isProp3");
        assertNotNull(method);

        method = type.getMethod("getPropFour");
        assertNotNull(method);
    }


    @Test
    public void testPropertiesAlternateNameResolves() {
        var type = TypeSystem.getType(DemoType.class);
        var prop = type.getProperty("prop_four");
        assertNotNull(prop);
    }

    public static class DemoType {

        // public field
        public String publicField = "public field";
        protected String protectedField = "protected field";
        private String privateField = "private field";

        // public method
        public String publicMethod() {
            return "public";
        }

        // protected method
        protected String protectedMethod() {
            return "protected";
        }

        // protected method
        private String privateMethod() {
            return "private";
        }

        // public method
        public static String publicStaticMethod() {
            return "public static";
        }

        // overloaded public method
        public String overloadedPublicMethod() {
            return "public";
        }

        public String overloadedPublicMethod(String s) {
            return "public " + s;
        }

        // overloaded void method
        public void foo() {}
        public void foo(String foo) {}


        // getters w/different visibilities
        public String getPublicString() {
            return "public string";
        }
        protected String getProtectedString() {
            return "protected string";
        }
        private String getPrivateString() {
            return "private string";
        }

        public String prop1() {
            return "prop1";
        }

        public String getProp2() {
            return "prop2";
        }

        public String isProp3() {
            return "isProp3";
        }

        public String getPropFour() {
            return "getPropFour";
        }
    }
}
