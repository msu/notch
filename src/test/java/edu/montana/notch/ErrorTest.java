package edu.montana.notch;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use in place of {@link Test} for a test that checks error message or diagnostic reporting.
 * These tests carry the {@value #TAG} tag so they can be filtered:
 * {@code mvn test -DexcludedGroups=errors} skips them, {@code mvn test -Dgroups=errors} runs only them.
 * Put {@code @Tag(ErrorTest.TAG)} on a class to tag every test in it.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("errors")
public @interface ErrorTest {
    String TAG = "errors";
}
