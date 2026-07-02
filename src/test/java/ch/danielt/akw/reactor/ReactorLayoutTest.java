package ch.danielt.akw.reactor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests für {@link ReactorLayout} — Assemblierungsprüfung und Weltkoordinaten-Umrechnung. */
class ReactorLayoutTest {

    private static ReactorLayout layout(int sizeX, int sizeY, int sizeZ, int coreCount) {
        return new ReactorLayout(
                -1, -1, -1,
                sizeX, sizeY, sizeZ,
                coreCount,
                0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    @DisplayName("isAssembled() ist wahr, wenn alle Achsen >= 3 sind und mindestens ein Kern existiert")
    void isAssembledBeiGueltigerHuelleUndKern() {
        assertTrue(layout(3, 3, 3, 1).isAssembled());
        assertTrue(layout(9, 9, 9, 4).isAssembled());
    }

    @Test
    @DisplayName("isAssembled() ist falsch, wenn eine Achse kleiner als 3 ist")
    void isAssembledFalschBeiZuKleinerAchse() {
        assertFalse(layout(2, 3, 3, 1).isAssembled());
        assertFalse(layout(3, 2, 3, 1).isAssembled());
        assertFalse(layout(3, 3, 2, 1).isAssembled());
    }

    @Test
    @DisplayName("isAssembled() ist falsch ohne Kern, selbst bei ausreichender Hüllengröße")
    void isAssembledFalschOhneKern() {
        assertFalse(layout(3, 3, 3, 0).isAssembled());
    }

    @Test
    @DisplayName("ReactorLayout.EMPTY ist niemals assembliert")
    void emptyIstNieAssembliert() {
        assertFalse(ReactorLayout.EMPTY.isAssembled());
    }

    @Test
    @DisplayName("maxDimension() liefert die größte der drei Kantenlängen")
    void maxDimensionLiefertGroessteKante() {
        assertEquals(9, layout(9, 3, 5, 1).maxDimension());
        assertEquals(9, layout(3, 9, 5, 1).maxDimension());
        assertEquals(9, layout(3, 5, 9, 1).maxDimension());
        assertEquals(3, layout(3, 3, 3, 1).maxDimension());
    }

    @Test
    @DisplayName("boundsMin() addiert die relative Minimal-Ecke auf die Controller-Position")
    void boundsMinAddiertRelativePosition() {
        ReactorLayout layout = new ReactorLayout(
                -2, -1, -3,
                5, 4, 6,
                1, 0, 0, 0, 0, 0, 0, 0, 0);
        BlockPos controller = new BlockPos(10, 64, 20);

        BlockPos min = layout.boundsMin(controller);

        assertEquals(new BlockPos(8, 63, 17), min);
    }

    @Test
    @DisplayName("boundsMax() addiert relative Minimal-Ecke plus Größe minus 1 auf die Controller-Position")
    void boundsMaxAddiertGroesseMinusEins() {
        ReactorLayout layout = new ReactorLayout(
                -2, -1, -3,
                5, 4, 6,
                1, 0, 0, 0, 0, 0, 0, 0, 0);
        BlockPos controller = new BlockPos(10, 64, 20);

        BlockPos max = layout.boundsMax(controller);

        // relMin(-2,-1,-3) + size(5,4,6) - 1 = (2,2,2) relativ zur Controller-Position.
        assertEquals(new BlockPos(12, 66, 22), max);
    }

    @Test
    @DisplayName("boundsMin() und boundsMax() liefern bei 3x3x3-Hülle zwei verschiedene, um 2 versetzte Ecken")
    void boundsMinUndMaxBeiKleinsterHuelle() {
        ReactorLayout layout = layout(3, 3, 3, 1);
        BlockPos controller = BlockPos.ZERO;

        BlockPos min = layout.boundsMin(controller);
        BlockPos max = layout.boundsMax(controller);

        assertEquals(new BlockPos(-1, -1, -1), min);
        assertEquals(new BlockPos(1, 1, 1), max);
    }
}
