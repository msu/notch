package edu.montana.notch.runtime;

public abstract class NotchControlFlow extends RuntimeException {
    public static final class Return extends NotchControlFlow {
        public final Object value;

        public Return(Object value) {
            this.value = value;
        }

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }

    public static final class Continue extends NotchControlFlow {
        public static final Continue INST = new Continue();

        private Continue() {}

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }

    public static final class Break extends NotchControlFlow {
        public static final Break INST = new Break();
        private Break() {}

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }

    // TODO:
//    public static final class Throw extends NotchControlFlow {
//        public final Throwable throwable;
//
//        public Throw(Throwable throwable) {
//            this.throwable = throwable;
//        }
//
//        @Override
//        public synchronized Throwable fillInStackTrace() {
//            return throwable;
//        }
//    }
}
