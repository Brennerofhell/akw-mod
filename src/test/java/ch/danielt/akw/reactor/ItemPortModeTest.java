package ch.danielt.akw.reactor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests für den next()-Zyklus und die Übersetzungs-Keys von {@link ItemPortMode}. */
class ItemPortModeTest {

    @Test
    @DisplayName("next() durchläuft alle drei Modi zyklisch und kehrt zum Ausgangswert zurück")
    void nextDurchlaeuftVollstaendigenZyklus() {
        assertSame(ItemPortMode.WASTE_OUTPUT, ItemPortMode.FUEL_INPUT.next());
        assertSame(ItemPortMode.DISABLED, ItemPortMode.WASTE_OUTPUT.next());
        assertSame(ItemPortMode.FUEL_INPUT, ItemPortMode.DISABLED.next());
    }

    @Test
    @DisplayName("translationKey() setzt sich aus Präfix und serialisiertem Namen zusammen")
    void translationKeyIstAbgeleitet() {
        assertEquals("akw.item_port.mode.fuel_input", ItemPortMode.FUEL_INPUT.translationKey());
        assertEquals("akw.item_port.mode.waste_output", ItemPortMode.WASTE_OUTPUT.translationKey());
        assertEquals("akw.item_port.mode.disabled", ItemPortMode.DISABLED.translationKey());
    }

    @Test
    @DisplayName("getSerializedName() liefert den BlockState-Wert für jeden Modus")
    void serializedNameIstBlockStateWert() {
        assertEquals("fuel_input", ItemPortMode.FUEL_INPUT.getSerializedName());
        assertEquals("waste_output", ItemPortMode.WASTE_OUTPUT.getSerializedName());
        assertEquals("disabled", ItemPortMode.DISABLED.getSerializedName());
    }
}
