package edu.montana.notch.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotchLoggingInstallTest {

    @AfterEach
    void resetState() {
        // default is AUTO; leave static flag in a known state for other tests.
        NotchLogging.install();   // flips to FORCE
        setMode(NotchLogging.Mode.AUTO);
        System.clearProperty(NotchLogging.DISABLE_PROPERTY);
    }

    private static void setMode(NotchLogging.Mode m) {
        try {
            var f = NotchLogging.class.getDeclaredField("mode");
            f.setAccessible(true);
            f.set(null, m);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void defaultEffectiveModeIsAuto() {
        setMode(NotchLogging.Mode.AUTO);
        System.clearProperty(NotchLogging.DISABLE_PROPERTY);
        assertEquals(NotchLogging.Mode.AUTO, NotchLogging.effectiveMode());
    }

    @Test
    void disablePropertySwitchesAutoToDisabled() {
        setMode(NotchLogging.Mode.AUTO);
        System.setProperty(NotchLogging.DISABLE_PROPERTY, "true");
        assertEquals(NotchLogging.Mode.DISABLED, NotchLogging.effectiveMode());
    }

    @Test
    void explicitForceBeatsDisableProperty() {
        setMode(NotchLogging.Mode.FORCE);
        System.setProperty(NotchLogging.DISABLE_PROPERTY, "true");
        assertEquals(NotchLogging.Mode.FORCE, NotchLogging.effectiveMode());
    }

    @Test
    void explicitDisableTakesPrecedenceOverMissingProperty() {
        setMode(NotchLogging.Mode.DISABLED);
        System.clearProperty(NotchLogging.DISABLE_PROPERTY);
        assertEquals(NotchLogging.Mode.DISABLED, NotchLogging.effectiveMode());
    }

    @Test
    void disableReturnsConfigAndSetsMode() {
        setMode(NotchLogging.Mode.AUTO);
        var cfg = NotchLogging.disable();
        assertEquals(NotchLogging.getConfig(), cfg);
        assertEquals(NotchLogging.Mode.DISABLED, NotchLogging.effectiveMode());
    }

    @Test
    void installReturnsConfigAndSetsMode() {
        setMode(NotchLogging.Mode.AUTO);
        var cfg = NotchLogging.install();
        assertEquals(NotchLogging.getConfig(), cfg);
        assertEquals(NotchLogging.Mode.FORCE, NotchLogging.effectiveMode());
    }
}
