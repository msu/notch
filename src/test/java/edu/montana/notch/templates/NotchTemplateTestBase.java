package edu.montana.notch.templates;

import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.Source;
import edu.montana.notch.templates.loader.InMemoryNotchTemplateLoader;
import edu.montana.notch.templates.runtime.RenderException;
import edu.montana.notch.util.Exceptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static edu.montana.notch.AssertContains.assertContains;
import static org.junit.jupiter.api.Assertions.*;

public abstract class NotchTemplateTestBase {
    protected Map<String, Source> templates = new HashMap<>();
    protected TestInfo testInfo;

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        this.testInfo = testInfo;
        this.templates = new HashMap<>();
    }

    protected String renderTemplate(String uri, Object... vars) {
        try {
            return renderTemplateForError(uri, vars);
        } catch (RenderException e) {
            var msg = e.render();
            Assertions.fail(msg, e);
            throw Exceptions.rethrow(e);
        }
    }

    protected String renderTemplateForError(String uri, Object... vars) {
        var varMap = new LinkedHashMap<String, Object>();
        assert vars.length % 2 == 0;
        for (int i = 0; i < vars.length; i += 2) {
            varMap.put((String) vars[i], vars[i + 1]);
        }

        final var loader = new InMemoryNotchTemplateLoader(templates);
        var sunnyTemplates = new NotchTemplates(loader);
        BasicNotchTemplateCommands.addTo(sunnyTemplates);
        return sunnyTemplates.renderTemplate(uri, varMap);
    }

    protected String renderString(String content, Object... vars) {
        registerTemplate(testInfo.getDisplayName(), content);
        return renderTemplate(testInfo.getDisplayName(), vars);
    }

    protected void registerTemplate(String name, String content) {
        templates.put(name, new Source(name, content));
    }

    void expectParseError(String uri, String expectedMessageFragment) {
        var templ = new NotchTemplates(new InMemoryNotchTemplateLoader(templates));
        BasicNotchTemplateCommands.addTo(templ);

        var ex = assertThrows(RenderException.class, () -> {
            templ.renderTemplate(uri, new LinkedHashMap<>());
        });

        assertFalse(ex.getChildren().isEmpty());
        var pe = assertInstanceOf(ParseException.class, ex.getChildren().get(0));

        if (expectedMessageFragment != null) {
            String message = pe.getMessage();
            assertContains(expectedMessageFragment, message, "Expected error message to contain '" + expectedMessageFragment + "' but got: " + message);
        }
    }

    void expectRenderError(String templateUri, String expectedMessageFragment, Object... args) {
        var argMap = new LinkedHashMap<String, Object>();
        for (int i = 0; i < args.length; i += 2) {
            var name = (String) args[i];
            var value = args[i + 1];
            argMap.put(name, value);
        }

        var templ = new NotchTemplates(new InMemoryNotchTemplateLoader(templates));
        BasicNotchTemplateCommands.addTo(templ);

        RenderException ex = assertThrows(RenderException.class, () -> {
            templ.renderTemplate(templateUri, argMap);
        }, "Expected RenderException but template rendered successfully");


        if (expectedMessageFragment != null) {
            String message = ex.getMessage();
            assertContains(expectedMessageFragment, message);
        }
    }

}
