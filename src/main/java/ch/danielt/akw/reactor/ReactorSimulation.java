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

    public static ReactorStats calculate(ReactorLayout layout, int activeCores) {
        if (!layout.isAssembled() || activeCores <= 0) {
            return new ReactorStats(0, 0, PASSIVE_COOLING, capacity(layout), maxHeat(layout));
        }

        int cores = layout.coreCount();
        int boundedActiveCores = Math.min(activeCores, cores);
        double neighborsPerCore = (double) layout.coreNeighborContacts() / cores;
        double reactivity = Math.min(1.0, 0.60 + 0.10 * neighborsPerCore);
        double controlRodsPerCore = Math.min(2.0, (double) layout.coreControlRodContacts() / cores);
        double heatFactor = Math.max(0.50, 1.0 - 0.25 * controlRodsPerCore);
        double coolingContactsPerCore = (double) layout.coreCoolingContacts() / cores;

        int generation = Math.max(1,
                (int) Math.round(boundedActiveCores * FE_PER_CORE * reactivity));
        int generatedHeat = Math.max(1,
                (int) Math.round(boundedActiveCores * HEAT_PER_CORE
                        * reactivity * reactivity * heatFactor));
        int cooling = PASSIVE_COOLING + (int) Math.round(
                boundedActiveCores * coolingContactsPerCore * COOLING_PER_CONTACT);

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
