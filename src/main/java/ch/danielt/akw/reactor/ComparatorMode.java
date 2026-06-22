package ch.danielt.akw.reactor;

/** Quelle des Komparator-Ausgangssignals (0–15) eines Reaktors. */
public enum ComparatorMode {
    ENERGY("akw.comparator_mode.energy"),
    TEMPERATURE("akw.comparator_mode.temperature"),
    FUEL("akw.comparator_mode.fuel"),
    WASTE("akw.comparator_mode.waste");

    private final String translationKey;

    ComparatorMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }

    public ComparatorMode next() {
        ComparatorMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
