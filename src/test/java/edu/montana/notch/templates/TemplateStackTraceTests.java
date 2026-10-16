package edu.montana.notch.templates;

import edu.montana.notch.runtime.NotchRuntimeException;
import edu.montana.notch.templates.runtime.NotchTemplateStackTraceElement;
import edu.montana.notch.templates.runtime.RenderException;
import edu.montana.notch.util.BetterList;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateStackTraceTests extends NotchTemplateTestBase {
    private BetterList<String> templateFrames(String uri) {
        var ex = assertThrows(RenderException.class, () -> renderTemplateForError(uri));
        var nre = assertInstanceOf(NotchRuntimeException.class, ex.getChildren().get(0));
        var frames = new BetterList<String>();
        for (var elt : nre.stackTrace.elements) {
            if (elt instanceof NotchTemplateStackTraceElement t) frames.add(t.report());
        }
        return frames;
    }

    @Test
    void nestedIncludesEachGetAFrame() {
        registerTemplate("main.html", "a\n#include \"mid.html\"\n");
        registerTemplate("mid.html", "b\n#for x in [1]\n#include \"leaf.html\"\n#end\n");
        registerTemplate("leaf.html", "c\n${java.lang.Integer.parseInt(\"nope\")}\n");

        assertEquals(List.of(
                "${...}(leaf.html:2)",
                "#include(mid.html:3)",
                "#for(mid.html:2)",
                "#include(main.html:2)"
        ), templateFrames("main.html"));
    }

    @Test
    void templateEngineFramesAreHidden() {
        registerTemplate("main.html", "#include \"leaf.html\"\n");
        registerTemplate("leaf.html", "${java.lang.Integer.parseInt(\"nope\")}\n");

        var ex = assertThrows(RenderException.class, () -> renderTemplateForError("main.html"));
        var nre = assertInstanceOf(NotchRuntimeException.class, ex.getChildren().get(0));
        var rendered = nre.stackTrace.render();
        assertFalse(rendered.contains("IncludeCommand"), rendered);
        assertFalse(rendered.contains("NotchTemplateContentBlock"), rendered);
    }
}
