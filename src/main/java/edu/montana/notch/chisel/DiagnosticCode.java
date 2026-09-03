package edu.montana.notch.chisel;

public interface DiagnosticCode {

    String id();

    String template();

    default String title(Object... args) {
        return args.length == 0 ? template() : template().formatted(args);
    }
}
