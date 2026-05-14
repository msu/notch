package edu.montana.notch.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionsTest {

    @Test
    void testRethrowWithRuntimeException() {
        RuntimeException original = new RuntimeException("Test runtime exception");
        
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            throw Exceptions.rethrow(original);
        });
        
        assertSame(original, thrown);
    }

    @Test
    void testRethrowWithCheckedException() {
        IOException original = new IOException("Test IO exception");
        
        IOException thrown = assertThrows(IOException.class, () -> {
            throw Exceptions.rethrow(original);
        });
        
        assertSame(original, thrown);
    }

    @Test
    void testRethrowWithError() {
        OutOfMemoryError original = new OutOfMemoryError("Test error");
        
        OutOfMemoryError thrown = assertThrows(OutOfMemoryError.class, () -> {
            throw Exceptions.rethrow(original);
        });
        
        assertSame(original, thrown);
    }

    @Test
    void testSafelyWithSuccessfulExecution() {
        StringBuilder result = new StringBuilder();
        
        assertDoesNotThrow(() -> {
            Exceptions.safely(() -> {
                result.append("executed");
            });
        });
        
        assertEquals("executed", result.toString());
    }

    @Test
    void testSafelyWithRuntimeException() {
        RuntimeException original = new RuntimeException("Test exception");
        
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            Exceptions.safely(() -> {
                throw original;
            });
        });
        
        assertSame(original, thrown);
    }

    @Test
    void testSafelyWithCheckedException() {
        IOException original = new IOException("Test IO exception");
        
        IOException thrown = assertThrows(IOException.class, () -> {
            Exceptions.safely(() -> {
                throw original;
            });
        });
        
        assertSame(original, thrown);
    }

    @Test
    void testSafelyEvalWithSuccessfulExecution() {
        Callable<String> callable = () -> "success";
        
        String result = assertDoesNotThrow(() -> Exceptions.safelyEval(callable));
        
        assertEquals("success", result);
    }

    @Test
    void testSafelyEvalWithIntegerReturn() {
        Callable<Integer> callable = () -> 42;
        
        Integer result = assertDoesNotThrow(() -> Exceptions.safelyEval(callable));
        
        assertEquals(Integer.valueOf(42), result);
    }

    @Test
    void testSafelyEvalWithRuntimeException() {
        RuntimeException original = new RuntimeException("Test exception");
        Callable<String> callable = () -> {
            throw original;
        };
        
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            Exceptions.safelyEval(callable);
        });
        
        assertSame(original, thrown);
    }

    @Test
    void testSafelyEvalWithCheckedException() {
        SQLException original = new SQLException("Test SQL exception");
        Callable<String> callable = () -> {
            throw original;
        };
        
        SQLException thrown = assertThrows(SQLException.class, () -> {
            Exceptions.safelyEval(callable);
        });
        
        assertSame(original, thrown);
    }

    @Test
    void testSafelyEvalWithAlternativeException() {
        IOException originalException = new IOException("Original exception");
        RuntimeException alternativeException = new RuntimeException("Alternative exception");
        
        Callable<String> callable = () -> {
            throw originalException;
        };
        
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            Exceptions.safelyEval(callable, alternativeException);
        });
        
        assertSame(alternativeException, thrown);
    }

    @Test
    void testSafelyEvalWithAlternativeExceptionSuccess() {
        RuntimeException alternativeException = new RuntimeException("Should not be thrown");
        Callable<String> callable = () -> "success";
        
        String result = assertDoesNotThrow(() -> {
            return Exceptions.safelyEval(callable, alternativeException);
        });
        
        assertEquals("success", result);
    }

    @Test
    void testSafelyEvalWithAlternativeAndNullReturn() {
        RuntimeException alternativeException = new RuntimeException("Alternative");
        Callable<String> callable = () -> null;
        
        String result = assertDoesNotThrow(() -> {
            return Exceptions.safelyEval(callable, alternativeException);
        });
        
        assertNull(result);
    }

    @Test
    void testRunnableWithExceptionInterface() {
        Exceptions.RunnableWithException runnable = () -> {
            throw new IOException("Test IO exception");
        };
        
        IOException thrown = assertThrows(IOException.class, () -> {
            Exceptions.safely(runnable);
        });
        
        assertEquals("Test IO exception", thrown.getMessage());
    }

    @Test
    void testMultipleLevelsOfExceptions() {
        SQLException rootCause = new SQLException("Root cause");
        IOException wrapperException = new IOException("Wrapper", rootCause);
        
        Callable<String> callable = () -> {
            throw wrapperException;
        };
        
        IOException thrown = assertThrows(IOException.class, () -> {
            Exceptions.safelyEval(callable);
        });
        
        assertSame(wrapperException, thrown);
        assertSame(rootCause, thrown.getCause());
    }

    @Test
    void testExceptionPreservation() {
        String originalMessage = "Original exception message";
        StackTraceElement[] originalStackTrace;
        
        try {
            throw new IllegalArgumentException(originalMessage);
        } catch (IllegalArgumentException original) {
            originalStackTrace = original.getStackTrace();
            
            IllegalArgumentException rethrown = assertThrows(IllegalArgumentException.class, () -> {
                throw Exceptions.rethrow(original);
            });
            
            assertEquals(originalMessage, rethrown.getMessage());
            assertArrayEquals(originalStackTrace, rethrown.getStackTrace());
        }
    }

    @Test
    void testSafelyWithComplexOperation() {
        StringBuilder log = new StringBuilder();
        
        assertDoesNotThrow(() -> {
            Exceptions.safely(() -> {
                log.append("step1;");
                Thread.sleep(1);
                log.append("step2;");
                if (Math.random() < 2.0) {
                    log.append("step3;");
                }
                log.append("completed");
            });
        });
        
        assertEquals("step1;step2;step3;completed", log.toString());
    }

    @Test
    void testNestedSafelyCalls() {
        StringBuilder result = new StringBuilder();
        
        assertDoesNotThrow(() -> {
            Exceptions.safely(() -> {
                result.append("outer_start;");
                Exceptions.safely(() -> {
                    result.append("inner;");
                });
                result.append("outer_end");
            });
        });
        
        assertEquals("outer_start;inner;outer_end", result.toString());
    }

    @Test
    void testSafelyEvalWithComplexCallable() {
        Callable<Integer> complexCallable = () -> {
            int sum = 0;
            for (int i = 1; i <= 5; i++) {
                sum += i;
            }
            return sum;
        };
        
        Integer result = assertDoesNotThrow(() -> {
            return Exceptions.safelyEval(complexCallable);
        });
        
        assertEquals(Integer.valueOf(15), result);
    }

    @Test
    void testErrorTypes() {
        Error error = new AssertionError("Test assertion error");
        
        AssertionError thrown = assertThrows(AssertionError.class, () -> {
            throw Exceptions.rethrow(error);
        });
        
        assertSame(error, thrown);
    }

    @Test
    void testCustomExceptionTypes() {
        class CustomCheckedException extends Exception {
            CustomCheckedException(String message) {
                super(message);
            }
        }
        
        CustomCheckedException original = new CustomCheckedException("Custom message");
        
        CustomCheckedException thrown = assertThrows(CustomCheckedException.class, () -> {
            throw Exceptions.rethrow(original);
        });
        
        assertSame(original, thrown);
        assertEquals("Custom message", thrown.getMessage());
    }
}