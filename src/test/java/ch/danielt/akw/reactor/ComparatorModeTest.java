package ch.danielt.akw.reactor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests für den next()-Zyklus von {@link ComparatorMode}. */
class ComparatorModeTest {

    @Test
    @DisplayName("next() durchläuft alle vier Modi zyklisch und kehrt zum Ausgangswert zurück")
    void nextDurchlaeuftVollstaendigenZyklus() {
        assertSame(ComparatorMode.TEMPERATURE, ComparatorMode.ENERGY.next());
        assertSame(ComparatorMode.FUEL, ComparatorMode.TEMPERATURE.next());
        assertSame(ComparatorMode.WASTE, ComparatorMode.FUEL.next());
        assertSame(ComparatorMode.ENERGY, ComparatorMode.WASTE.next());
    }

    @Test
    @DisplayName("translationKey() liefert für jeden Modus den erwarteten Übersetzungs-Key")
    void translationKeyIstKorrekt() {
        assertEquals("akw.comparator_mode.energy", ComparatorMode.ENERGY.translationKey());
        assertEquals("akw.comparator_mode.temperature", ComparatorMode.TEMPERATURE.translationKey());
        assertEquals("akw.comparator_mode.fuel", ComparatorMode.FUEL.translationKey());
        assertEquals("akw.comparator_mode.waste", ComparatorMode.WASTE.translationKey());
    }
}
