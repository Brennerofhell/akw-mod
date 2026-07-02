package ch.danielt.akw.reactor;

import net.minecraft.util.StringRepresentable;

/** Betriebsmodus eines Reaktor-Item-Ports (BlockState-Property, per Rechtsklick umschaltbar). */
public enum ItemPortMode implements StringRepresentable {
    FUEL_INPUT("fuel_input"),
    WASTE_OUTPUT("waste_output"),
    DISABLED("disabled");

    private final String serializedName;

    ItemPortMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public String translationKey() {
        return "akw.item_port.mode." + serializedName;
    }

    public ItemPortMode next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
