package edu.montana.notch.templates.runtime;

import edu.montana.notch.templates.NotchTemplateCommand;

import java.util.Map;

public interface NotchTemplateImportable extends NotchTemplateCommand.Global {
    Map<String, Object> getExportedValues();
}
