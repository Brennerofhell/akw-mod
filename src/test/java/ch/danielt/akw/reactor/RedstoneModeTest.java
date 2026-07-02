package ch.danielt.akw.reactor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests für den next()-Zyklus von {@link RedstoneMode}. */
class RedstoneModeTest {

    @Test
    @DisplayName("next() durchläuft alle vier Modi zyklisch und kehrt zum Ausgangswert zurück")
    void nextDurchlaeuftVollstaendigenZyklus() {
        assertSame(RedstoneMode.HIGH_ENABLES, RedstoneMode.IGNORED.next());
        assertSame(RedstoneMode.HIGH_DISABLES, RedstoneMode.HIGH_ENABLES.next());
        assertSame(RedstoneMode.EMERGENCY_STOP, RedstoneMode.HIGH_DISABLES.next());
        assertSame(RedstoneMode.IGNORED, RedstoneMode.EMERGENCY_STOP.next());
    }

    @Test
    @DisplayName("translationKey() liefert für jeden Modus den erwarteten Übersetzungs-Key")
    void translationKeyIstKorrekt() {
        assertEquals("akw.redstone_mode.ignored", RedstoneMode.IGNORED.translationKey());
        assertEquals("akw.redstone_mode.high_enables", RedstoneMode.HIGH_ENABLES.translationKey());
        assertEquals("akw.redstone_mode.high_disables", RedstoneMode.HIGH_DISABLES.translationKey());
        assertEquals("akw.redstone_mode.emergency_stop", RedstoneMode.EMERGENCY_STOP.translationKey());
    }
}
