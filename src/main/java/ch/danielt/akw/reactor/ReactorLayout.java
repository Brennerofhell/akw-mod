package ch.danielt.akw.reactor;

import net.minecraft.core.BlockPos;

/**
 * Unveränderliche Zusammenfassung eines assemblierten Reaktor-Innenraums.
 * Weltzugriffe finden nur während der Validierung statt; der normale Tick nutzt
 * ausschließlich diese vorberechneten Werte. Die Hülle ist ein rechteckiger Quader;
 * {@code relMin*} beschreibt die Minimal-Ecke relativ zur Controller-Position.
 */
public record ReactorLayout(
        int relMinX,
        int relMinY,
        int relMinZ,
        int sizeX,
        int sizeY,
        int sizeZ,
        int coreCount,
        int controlRodCount,
        int coolingPipeCount,
        int connectedCoolingPipeCount,
        int coreNeighborContacts,
        int coreControlRodContacts,
        int coreCoolingContacts,
        int energyPortCount,
        int itemPortCount) {

    public static final ReactorLayout EMPTY =
            new ReactorLayout(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

    public boolean isAssembled() {
        return sizeX >= 3 && sizeY >= 3 && sizeZ >= 3 && coreCount > 0;
    }

    /** Größte Kantenlänge der Hülle (z. B. für den Explosionsradius). */
    public int maxDimension() {
        return Math.max(sizeX, Math.max(sizeY, sizeZ));
    }

    /** Minimal-Ecke der Hülle in Weltkoordinaten. */
    public BlockPos boundsMin(BlockPos controllerPos) {
        return controllerPos.offset(relMinX, relMinY, relMinZ);
    }

    /** Maximal-Ecke der Hülle in Weltkoordinaten. */
    public BlockPos boundsMax(BlockPos controllerPos) {
        return controllerPos.offset(relMinX + sizeX - 1, relMinY + sizeY - 1, relMinZ + sizeZ - 1);
    }
}
