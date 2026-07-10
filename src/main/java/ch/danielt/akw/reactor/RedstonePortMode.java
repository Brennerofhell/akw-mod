package ch.danielt.akw.reactor;

import net.minecraft.util.StringRepresentable;

public enum RedstonePortMode implements StringRepresentable {
    INPUT("input", "akw.redstone_port_mode.input"),
    OUTPUT("output", "akw.redstone_port_mode.output");

    private final String name;
    private final String translationKey;

    RedstonePortMode(String name, String translationKey) {
        this.name = name;
        this.translationKey = translationKey;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public String translationKey() {
        return translationKey;
    }

    public RedstonePortMode next() {
        RedstonePortMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
