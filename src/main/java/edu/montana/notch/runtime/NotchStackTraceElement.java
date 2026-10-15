package edu.montana.notch.runtime;


import edu.montana.notch.chisel.Span;

public interface NotchStackTraceElement {
    String report();

    class Notch implements NotchStackTraceElement {
        public final Span callsite;
        public final String functionName;

        public Notch(Span callsite, String functionName) {
            this.callsite = callsite;
            this.functionName = functionName;
        }

        @Override
        public String report() {
            return "%s(%s:%d)".formatted(functionName, callsite.source().id, callsite.startLine());
        }
    }

    class Java implements NotchStackTraceElement {
        public final StackTraceElement javaTraceElement;

        public Java(StackTraceElement element) {
            this.javaTraceElement = element;
        }

        @Override
        public String report() {
            return javaTraceElement.toString();
        }
    }
}