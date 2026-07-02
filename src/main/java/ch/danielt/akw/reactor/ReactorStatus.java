package ch.danielt.akw.reactor;

/** Betriebszustand des Multiblock-Reaktors (Zustandsautomat im Controller-Tick). */
public enum ReactorStatus {
    UNASSEMBLED,
    OFFLINE,
    STARTING,
    RUNNING,
    SCRAM,
    COOLDOWN,
    DAMAGED;

    public String translationKey() {
        return "akw.reactor.status." + name().toLowerCase(java.util.Locale.ROOT);
    }

    public static ReactorStatus byOrdinal(int ordinal, ReactorStatus fallback) {
        return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : fallback;
    }
}
