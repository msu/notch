package bigsky.notch.templates;

import bigsky.notch.templates.loader.NotchTemplateLoader;
import bigsky.notch.templates.runtime.RenderException;
import bigsky.notch.util.Exceptions;
import bigsky.notch.util.Text;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public abstract class NotchTemplateTestBase {
    protected Map<String, String> templates = new HashMap<>();
    protected TestTemplateLoader loader = new TestTemplateLoader();
    protected TestInfo testInfo;

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        this.testInfo = testInfo;
        this.templates = new HashMap<>();
    }

    protected String renderTemplate(String mainTemplate, Object... vars) {
        try {
            return renderTemplateForError(mainTemplate, vars);
        } catch (RenderException e) {
            var msg = e.render(loader);
            System.out.println(msg);
            Assertions.fail(e);
            throw Exceptions.rethrow(e);
        }
    }

    protected String renderTemplateForError(String mainTemplate, Object... vars) {
        var varMap = new LinkedHashMap<String, Object>();
        assert vars.length % 2 == 0;
        for (int i = 0; i < vars.length; i += 2) {
            varMap.put((String) vars[i], vars[i + 1]);
        }

        var sunnyTemplates = new NotchTemplateRegistry(loader);
        BasicNotchTemplateCommands.addTo(sunnyTemplates);

        return sunnyTemplates.renderTemplate(mainTemplate, varMap);
    }

    protected String renderString(String content, Object... vars) {
        registerTemplate(testInfo.getDisplayName(), content);
        return renderTemplate(testInfo.getDisplayName(), vars);
    }

    protected void registerTemplate(String name, String content) {
        templates.put(name, content);
    }

    protected String getTemplate(String name) {
        return Objects.requireNonNull(templates.get(name), "no such template " + Text.repr(name));
    }

    public class TestTemplateLoader extends NotchTemplateLoader {
        @Override
        public String loadTemplate(String path) {
            return getTemplate(path);
        }
    }
}
