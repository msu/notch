package edu.montana.notch.templates;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FragmentTests extends NotchTemplateTestBase {
    @Test
    public void wholeTemplate() {
        var result = renderString("""
                <body>
                <h1>Title</h1>
                #fragment content
                <div class="content">
                    <p>Hello!</p>
                </div>
                #end
                </body>
                """);

        assertEquals("""
                <body>
                <h1>Title</h1>
                <div class="content">
                    <p>Hello!</p>
                </div>
                </body>
                """, result);
    }

    @Test
    public void fragmentTemplate() {
        registerTemplate("main.html", """
                <body>
                <h1>Title</h1>
                #fragment content
                <div class="content">
                    <p>Hello!</p>
                </div>
                #end
                </body>
                """);

        String result = renderTemplate("main.html#content");
        assertEquals("""
                <div class="content">
                    <p>Hello!</p>
                </div>
                """, result);
    }

    @Test
    public void foreignFragment() {
        registerTemplate("view.html", """
                <body>
                <h1>Title</h1>
                #fragment content
                    <div class="content">
                    <p>Hello!</p>
                    </div>
                #end
                </body>
                """);

        registerTemplate("update.html", """
                #import "view.html" as view
                
                <div hx-swap-oob="#dingus">
                #expand view.content()
                </div>
                """);

        var result = renderTemplate("update.html");
        assertEquals("""
                
                <div hx-swap-oob="#dingus">
                    <div class="content">
                    <p>Hello!</p>
                    </div>
                </div>
                """, result);
    }

    @Test
    public void fragmentWithVars() {
        registerTemplate("view.html", """
                <body>
                #fragment content
                <p>${name}</p>
                #end
                </body>
                """);

        registerTemplate("update.html", """
                #import "view.html" as view
                <div hx-swap-oob="#dingus">
                #expand view.content()
                </div>
                """);

        var result = renderTemplate("view.html", "name", "Dillon");
        assertEquals("""
                <body>
                <p>Dillon</p>
                </body>
                """, result);

        result = renderTemplate("update.html", "name", "Dillon");
        assertEquals("""
                <div hx-swap-oob="#dingus">
                <p>Dillon</p>
                </div>
                """, result);
    }
}
