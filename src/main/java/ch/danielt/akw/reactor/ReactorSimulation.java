package ch.danielt.akw.reactor;

/** Reine, weltunabhängige Balanceberechnung für den modularen Reaktor. */
public final class ReactorSimulation {
    public static final int FE_PER_CORE = 80;
    public static final int HEAT_PER_CORE = 12;
    public static final int BURN_TICKS = 2_400;
    public static final int CAPACITY_PER_CORE = 100_000;
    public static final int PASSIVE_COOLING = 2;
    public static final int COOLING_PER_CONTACT = 8;

    private ReactorSimulation() {
    }

    /**
     * @param controlRodInsertion stufenloser Steuerstabwert 0–100 %;
     *                            Endreaktivität = Grundreaktivität × (1 − Einschub/100).
     */
    public static ReactorStats calculate(ReactorLayout layout, int activeCores, int controlRodInsertion) {
        if (!layout.isAssembled()) {
            return new ReactorStats(0, 0, PASSIVE_COOLING, capacity(layout), maxHeat(layout));
        }

        int cores = layout.coreCount();
        // Kühlrohre kühlen die Struktur unabhängig vom Brennzustand — sonst würde
        // SCRAM/COOLDOWN (activeCores=0) die installierte Kühlung wirkungslos machen
        // und die Nachzerfallswärme könnte trotz Kühlrohren nicht abgeführt werden.
        double coolingContactsPerCore = (double) layout.coreCoolingContacts() / cores;
        int cooling = PASSIVE_COOLING + (int) Math.round(
                cores * coolingContactsPerCore * COOLING_PER_CONTACT);

        if (activeCores <= 0) {
            return new ReactorStats(0, 0, cooling, capacity(layout), maxHeat(layout));
        }

        int boundedActiveCores = Math.min(activeCores, cores);
        double neighborsPerCore = (double) layout.coreNeighborContacts() / cores;
        double baseReactivity = Math.min(1.0, 0.60 + 0.10 * neighborsPerCore);
        double insertion = Math.clamp(controlRodInsertion, 0, 100) / 100.0;
        double reactivity = baseReactivity * (1.0 - insertion);
        double controlRodsPerCore = Math.min(2.0, (double) layout.coreControlRodContacts() / cores);
        double heatFactor = Math.max(0.50, 1.0 - 0.25 * controlRodsPerCore);
        double graphiteContactsPerCore = (double) layout.coreGraphiteContacts() / cores;
        double graphiteFactor = Math.pow(0.75, graphiteContactsPerCore);

        int generation = Math.max(0,
                (int) Math.round(boundedActiveCores * FE_PER_CORE * reactivity * (1.0 + 0.20 * graphiteContactsPerCore)));
        int generatedHeat = Math.max(0,
                (int) Math.round(boundedActiveCores * HEAT_PER_CORE
                        * reactivity * reactivity * heatFactor * graphiteFactor));

        return new ReactorStats(generation, generatedHeat, cooling, capacity(layout), maxHeat(layout));
    }

    public static int capacity(ReactorLayout layout) {
        return Math.max(CAPACITY_PER_CORE,
                Math.multiplyExact(Math.max(1, layout.coreCount()), CAPACITY_PER_CORE));
    }

    public static int maxHeat(ReactorLayout layout) {
        return 1_600 + Math.max(1, layout.coreCount()) * 200;
    }

    public record ReactorStats(
            int generationPerTick,
            int heatPerTick,
            int coolingPerTick,
            int capacity,
            int maxHeat) {
    }
}
