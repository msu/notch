package bigsky.notch.types;

public class DemoType {

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

}
