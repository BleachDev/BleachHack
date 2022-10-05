package org.bleachhack.util.doom.utils;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public enum Throwers {
    ;
        @SafeVarargs
    public static <T> Callable<T>
            callable(ThrowingCallable<T> r, Class<? extends Throwable>... cl) throws Throwed {
        return () -> {
            try {
                return r.call();
            } catch (Throwable e) {
                if (classifyMatching(e, cl)) {
                    throw doThrow(e);
                } else {
                    throw doThrowE(e);
                }
            }
        };
    }

    @SafeVarargs
    public static Runnable
            runnable(ThrowingRunnable r, Class<? extends Throwable>... cl) throws Throwed {
        return () -> {
            try {
                r.run();
            } catch (Throwable e) {
                if (classifyMatching(e, cl)) {
                    throw doThrow(e);
                } else {
                    throw doThrowE(e);
                }
            }
        };
    }

    @SafeVarargs
    public static <T> Consumer<T>
            consumer(ThrowingConsumer<T> c, Class<? extends Throwable>... cl) throws Throwed {
        return (t) -> {
            try {
                c.accept(t);
            } catch (Throwable e) {
                if (classifyMatching(e, cl)) {
                    throw doThrow(e);
                } else {
                    throw doThrowE(e);
                }
            }
        };
    }

    @SafeVarargs
    public static <T1, T2> BiConsumer<T1, T2>
            biConsumer(ThrowingBiConsumer<T1, T2> c, Class<? extends Throwable>... cl) throws Throwed {
        return (t1, t2) -> {
            try {
                c.accept(t1, t2);
            } catch (Throwable e) {
                if (classifyMatching(e, cl)) {
                    throw doThrow(e);
                } else {
                    throw doThrowE(e);
                }
            }
        };
    }

    @SafeVarargs
    public static <T> Predicate<T>
            predicate(ThrowingPredicate<T> p, Class<? extends Throwable>... cl) throws Throwed {
        return (t) -> {
            try {
                return p.test(t);
            } catch (Throwable e) {
                if (classifyMatching(e, cl)) {
                    throw doThrow(e);
                } else {
                    throw doThrowE(e);
                }
            }
        };
    }

    @SafeVarargs
    public static <T1, T2> BiPredicate<T1, T2>
            biPredicate(ThrowingBiPredicate<T1, T2> p, Class<? extends Throwable>... cl) throws Throwed {
        return (t1, t2) -> {
            try {
                return p.test(t1, t2);
            } catch (Throwable e) {
                if (classifyMatching(e, cl)) {
                    throw doThrow(e);
                } else {
                    throw doThrowE(e);
                }
            }
        };
    }

    @SafeVarargs
    public static <T, R> Function<T, R>
            function(ThrowingFunction<T, R> f, Class<? extends Throwable>... cl) throws Throwed {
        return (t) -> {
            try {
                return f.apply(t);
            } catch (Throwable e) {
                if (classifyMatching(e, cl)) {
                    throw doThrow(e);
                } else {
                    throw doThrowE(e);
                }
            }
        };
    }

    @SafeVarargs
    public static <T1, T2, R> BiFunction<T1, T2, R>
            biFunction(ThrowingBiFunction<T1, T2, R> f, Class<? extends Throwable>... cl) throws Throwed {
        return (t1, t2) -> {
            try {
                return f.apply(t1, t2);
            } catch (Throwable e) {
                if (classifyMatching(e, cl)) {
                    throw doThrow(e);
                } else {
                    throw doThrowE(e);
                }
            }
        };
    }

    @SafeVarargs
    public static <T> Supplier<T>
            supplier(ThrowingSupplier<T> s, Class<? extends Throwable>... cl) throws Throwed {
        return () -> {
            try {
                return s.get();
            } catch (Throwable e) {
                if (classifyMatching(e, cl)) {
                    throw doThrow(e);
                } else {
                    throw doThrowE(e);
                }
            }
        };
    }

    public static class Throwed extends RuntimeException {

        private static final long serialVersionUID = 5802686109960804684L;
        public final Throwable t;

        private Throwed(Throwable t) {
            super(null, null, true, false);
            this.t = t;
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return t.fillInStackTrace();
        }

        @Override
        public synchronized Throwable getCause() {
            return t.getCause();
        }

        @Override
        public String getLocalizedMessage() {
            return t.getLocalizedMessage();
        }

        @Override
        public String getMessage() {
            return t.getMessage();
        }

        @Override
        public StackTraceElement[] getStackTrace() {
            return t.getStackTrace();
        }

        @Override
        public void setStackTrace(StackTraceElement[] stackTrace) {
            t.setStackTrace(stackTrace);
        }

        @Override
        public synchronized Throwable initCause(Throwable cause) {
            return t.initCause(cause);
        }

        @Override
        @SuppressWarnings("CallToPrintStackTrace")
        public void printStackTrace() {
            t.printStackTrace();
        }

        @Override
        public void printStackTrace(PrintStream s) {
            t.printStackTrace(s);
        }

        @Override
        public void printStackTrace(PrintWriter s) {
            t.printStackTrace(s);
        }

        @Override
        public String toString() {
            return t.toString();
        }
    }

    public interface ThrowingCallable<T> {

        T call() throws Throwable;
    }

    public interface ThrowingRunnable {

        void run() throws Throwable;
    }

    public interface ThrowingConsumer<T> {

        void accept(T t) throws Throwable;
    }

    public interface ThrowingBiConsumer<T1, T2> {

        void accept(T1 t1, T2 t2) throws Throwable;
    }

    public interface ThrowingPredicate<T> {

        boolean test(T t) throws Throwable;
    }

    public interface ThrowingBiPredicate<T1, T2> {

        boolean test(T1 t1, T2 t2) throws Throwable;
    }

    public interface ThrowingFunction<T, R> {

        R apply(T t) throws Throwable;
    }

    public interface ThrowingBiFunction<T1, T2, R> {

        R apply(T1 t1, T2 t2) throws Throwable;
    }

    public interface ThrowingSupplier<T> {

        T get() throws Throwable;
    }

    /**
     * Throw checked exception as runtime exception preserving stack trace The class of exception will be changed so it
     * will only trigger catch statements for new type
     *
     * @param e exception to be thrown
     * @return impossible
     * @throws Throwed
     */
    public static RuntimeException doThrow(final Throwable e) throws Throwed {
        throw new Throwed(e);
    }

    /**
     * Throw checked exception as runtime exception preserving stack trace The class of exception will not be changed.
     * In example, an InterruptedException would then cause a Thread to be interrupted
     *
     * @param <E>
     * @param e exception to be thrown
     * @return impossible
     * @throws E (in runtime)
     */
    @SuppressWarnings("unchecked")
    private static <E extends Throwable> RuntimeException doThrowE(final Throwable e) throws E {
        throw (E) e;
    }

    @SafeVarargs
    private static boolean classifyMatching(Throwable ex, Class<? extends Throwable>... options) {
        for (Class<? extends Throwable> o : options) {
            if (o.isInstance(ex)) {
                return true;
            }
        }

        return false;
    }
}
