package edu.montana.notch.util;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;

public final class BetterSystem {
    private BetterSystem() {
    }

    public enum Arch {
        X86,
        ARM,
        OTHER,
    }

    public enum OperatingSystem {
        WINDOWS,
        MACOS,
        LINUX,
        OTHER,
    }

    public static Arch getArch() {
        var arch = System.getProperty("os.arch").toLowerCase();

        if (arch.contains("x86") || arch.contains("amd")) {
            return Arch.X86;
        }

        if (arch.contains("aarch64") || arch.contains("arm")) {
            return Arch.ARM;
        }

        return Arch.OTHER;
    }

    public static OperatingSystem os() {
        var name = System.getProperty("os.name").toLowerCase();

        if (name.contains("nux") || name.contains("nix"))
            return OperatingSystem.LINUX;
        if (name.contains("macos") || name.contains("darwin"))
            return OperatingSystem.MACOS;
        if (name.contains("win"))
            return OperatingSystem.WINDOWS;

        return OperatingSystem.OTHER;
    }

    public static String getIPAddress() {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            String ip = socket.getLocalAddress().getHostAddress();
            return ip;
        } catch (Exception e) {
            // swallow
        }
        return null;
    }

    public static int findOpenPort(int targetPort) {
        var port = targetPort;
        while (port < 65536) {
            if (isPortAvailable(port)) {
                return port;
            }
            port++;
        }
        throw new IllegalStateException("No ports available above " + targetPort);
    }

    public static boolean isPortAvailable(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}


