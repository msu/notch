package edu.montana.notch.templates;

import bigsky.notch.templates.command.*;
import edu.montana.notch.templates.command.*;

public final class BasicNotchTemplateCommands {
    private BasicNotchTemplateCommands() {
    }

    public static final NotchTemplateCommand[] COMMANDS = new NotchTemplateCommand[]{
            new EndCommand(),
            new ElseCommand(),
            new HelperCommand(),
            new ContentCommand(),
            new LayoutCommand(),
            new ForCommand(),
            new IfCommand(),
            new ElseIfCommand(),
            new IncludeCommand(),
            new MacroCommand(),
            new ExpandCommand(),
            new ImportCommand(),
            new FragmentCommand(),
            new SetCommand(),
            new CommentCommand(),
            new RequireCommand(),
    };

    public static void addTo(NotchTemplateRegistry templates) {
        for (var cmd : COMMANDS) {
            if (!templates.hasCommand(cmd)) {
                templates.addCommand(cmd);
            }
        }
    }
}
