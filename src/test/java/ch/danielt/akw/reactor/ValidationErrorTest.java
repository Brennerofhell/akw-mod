package ch.danielt.akw.reactor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.danielt.akw.reactor.ValidationError.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Tests für {@link ValidationError} und {@link ValidationError.Type}. */
class ValidationErrorTest {

    @ParameterizedTest(name = "{0} blockiert die Assemblierung")
    @EnumSource(value = Type.class, names = {"GAP", "FOREIGN_BLOCK", "NO_CORE", "NO_ENERGY_PORT", "TOO_LARGE"})
    @DisplayName("Alle Fehlertypen außer DISCONNECTED_PIPE blockieren die Assemblierung")
    void blockierendeFehlertypen(Type type) {
        assertTrue(type.blocksAssembly());
    }

    @Test
    @DisplayName("DISCONNECTED_PIPE blockiert die Assemblierung NICHT")
    void disconnectedPipeBlockiertNicht() {
        assertFalse(Type.DISCONNECTED_PIPE.blocksAssembly());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Type.class)
    @DisplayName("translationKey() beginnt bei allen Typen mit \"akw.reactor.error.\"")
    void translationKeyHatErwartetesPraefix(Type type) {
        assertTrue(type.translationKey().startsWith("akw.reactor.error."));
    }

    @Test
    @DisplayName("toComponent() erzeugt eine übersetzbare Komponente mit korrektem Key und Koordinaten-Argumenten")
    void toComponentEnthaeltKeyUndKoordinaten() {
        BlockPos pos = new BlockPos(4, 70, -3);
        ValidationError error = new ValidationError(Type.GAP, pos);

        var component = error.toComponent();

        assertNotNull(component);
        assertTrue(component.getContents() instanceof TranslatableContents);
        TranslatableContents contents = (TranslatableContents) component.getContents();
        assertEquals("akw.reactor.error.gap", contents.getKey());
    }

    @Test
    @DisplayName("pos() liefert exakt die im Konstruktor übergebene Position zurück")
    void posLiefertKonstruktorPosition() {
        BlockPos pos = new BlockPos(1, 2, 3);
        ValidationError error = new ValidationError(Type.NO_CORE, pos);

        assertEquals(pos, error.pos());
        assertEquals(Type.NO_CORE, error.type());
    }
}
