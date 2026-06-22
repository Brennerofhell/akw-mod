package ch.danielt.akw.reactor;

/**
 * Unveränderliche Zusammenfassung eines assemblierten Reaktor-Innenraums.
 * Weltzugriffe finden nur während der Validierung statt; der normale Tick nutzt
 * ausschließlich diese vorberechneten Werte.
 */
public record ReactorLayout(
        int outerSize,
        int coreCount,
        int controlRodCount,
        int coolingPipeCount,
        int connectedCoolingPipeCount,
        int coreNeighborContacts,
        int coreControlRodContacts,
        int coreCoolingContacts) {

    public static final ReactorLayout EMPTY = new ReactorLayout(0, 0, 0, 0, 0, 0, 0, 0);

    public boolean isAssembled() {
        return outerSize >= 3 && coreCount > 0;
    }
}
