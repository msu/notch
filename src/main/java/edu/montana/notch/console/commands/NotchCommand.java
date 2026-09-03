package edu.montana.notch.console.commands;

import edu.montana.notch.Notch;
import edu.montana.notch.chisel.*;
import edu.montana.notch.console.ShellContext;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.runtime.NotchRuntimeException;
import org.jline.reader.LineReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public final class NotchCommand {

    private static final String RUNTIME_VAR = "notchRuntime";
    private static final String CONTEXT_VAR = "shellContext";

    private NotchCommand() {}

    public static void storeContext(LineReader reader, ShellContext ctx) {
        reader.setVariable(CONTEXT_VAR, ctx);
    }

    private static ShellContext getContext(LineReader reader) {
        Object object = reader.getVariable(CONTEXT_VAR);
        return object instanceof ShellContext ctx ? ctx : null;
    }

    public static void run(LineReader reader, String line) {
        runWithFileId(reader, new Source("jackknife-cli", line), true);
    }

    public static void runWithoutRecording(LineReader reader, Source source) {
        runWithFileId(reader, source, false);
    }

    private static void runWithFileId(LineReader reader, Source source, boolean recordOnSuccess) {
        NotchRuntime runtime = lookupOrCreateRuntime(reader);

        try {
            Object result = Notch.run(source, runtime);
            if (result != null && !runtime.isUndefined(result)) {
                System.out.println(result);
            }
            if (recordOnSuccess) appendIfRecording(reader, source);
        } catch (ParseErrors e) {
            StringBuilder shellErrorMessage = new StringBuilder();
            for (Diagnostic diagnostic : e.diagnostics) {
                if (!shellErrorMessage.isEmpty()) shellErrorMessage.append("\n");
                shellErrorMessage.append(diagnostic.render(false));
            }
            System.out.println(shellErrorMessage);
        } catch (Exception e) {
            System.out.println(userFacingMessage(e));
        }
    }

    private static String userFacingMessage(Throwable e) {
        if (e instanceof NotchRuntimeException nre) {
            return nre.diagnostic.render(false);
        } else if (e instanceof ParseException pe) {
            return pe.diagnostic.render(false);
        } else if (e instanceof TokenizeException te) {
            return te.diagnostic.render(false);
        }
        return e.getMessage();
    }

    private static void appendIfRecording(LineReader reader, Source source) {
        ShellContext ctx = getContext(reader);
        if (ctx == null || ctx.recording == null) return;
        try (BufferedWriter writer = Files.newBufferedWriter(
                ctx.recording, StandardOpenOption.APPEND)) {
            writer.write(source.content.toString());
            writer.newLine();
            ctx.recordingCount++;
        } catch (IOException e) {
            System.out.println("write: append failed: " + e.getMessage());
        }
    }

    private static NotchRuntime lookupOrCreateRuntime(LineReader reader) {
        Object existing = reader.getVariable(RUNTIME_VAR);
        if (existing instanceof NotchRuntime nr) return nr;
        NotchRuntime created = new NotchRuntime(new Source("jackknife-cli", ""));
        reader.setVariable(RUNTIME_VAR, created);
        return created;
    }

    public static void resetRuntime(LineReader reader) {
        reader.setVariable(RUNTIME_VAR, null);
    }
}
