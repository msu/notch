package bigsky.notch.templates.runtime;

import bigsky.notch.templates.NotchTemplateCommand;

import java.util.Map;

public interface NotchTemplateImportable extends NotchTemplateCommand.Global {
    Map<String, Object> getExportedValues();
}
