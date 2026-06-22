package ch.danielt.akw.reactor;

/** Konfigurierbare Reaktion auf ein Redstone-Signal am Reaktor-Block. */
public enum RedstoneMode {
    IGNORED("akw.redstone_mode.ignored"),
    HIGH_ENABLES("akw.redstone_mode.high_enables"),
    HIGH_DISABLES("akw.redstone_mode.high_disables"),
    EMERGENCY_STOP("akw.redstone_mode.emergency_stop");

    private final String translationKey;

    RedstoneMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }

    public RedstoneMode next() {
        RedstoneMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
