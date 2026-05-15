package edu.montana.notch.console.commands;

import edu.montana.notch.logging.NotchLogging;
import picocli.CommandLine.Command;

@Command(name = "logs", description = "Show background-captured logs.")
public class LogsCommand implements Runnable {

    @Override
    public void run() {
        System.out.println(NotchLogging.getConfig().takeBackgroundLogs());
    }
}
