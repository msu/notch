package edu.montana.notch.templates;

import edu.montana.notch.templates.command.*;

public final class BasicNotchTemplateCommands {
    private BasicNotchTemplateCommands() {
    }

    public static final NotchTemplateCommand[] COMMANDS = new NotchTemplateCommand[]{
            new HelperCommand(),
            new ContentCommand(),
            new LayoutCommand(),
            new ForCommand(),
            new IfCommand(),
            new IncludeCommand(),
            new MacroCommand(),
            new ExpandCommand(),
            new ImportCommand(),
            new FragmentCommand(),
            new SetCommand(),
            new CommentCommand(),
            new RequireCommand(),
            new RawCommand(),
    };

    public static void addTo(NotchTemplates templates) {
        for (var cmd : COMMANDS) {
            if (!templates.hasCommand(cmd)) {
                templates.addCommand(cmd);
            }
        }
    }
}
