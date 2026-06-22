package ch.danielt.akw.block.entity;

import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

/**
 * NeoForge {@link SimpleEnergyHandler}-Subklasse (neue Transfer-API, 1.21.10),
 * die direktes Setzen des Energiebetrags und eine schlanke, IEnergyStorage-artige
 * Fassade bietet. Als {@code EnergyHandler} kann die Instanz direkt unter
 * {@code Capabilities.Energy.BLOCK} registriert werden.
 */
public class MutableEnergyStorage extends SimpleEnergyHandler {

    private Runnable onChange = () -> { };

    public MutableEnergyStorage(int capacity, int maxTransfer) {
        super(capacity, maxTransfer, maxTransfer);
    }

    public MutableEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    /** Callback, der bei jeder Energieänderung läuft (z. B. {@code BlockEntity::setChanged}). */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange == null ? () -> { } : onChange;
    }

    @Override
    protected void onEnergyChanged(int diff) {
        onChange.run();
    }

    // --- IEnergyStorage-artige Fassade (von den BlockEntities genutzt) ---

    /** Aktuell gespeicherte Energie. */
    public int getEnergyStored() {
        return getAmountAsInt();
    }

    /** Maximal speicherbare Energie. */
    public int getMaxEnergyStored() {
        return getCapacityAsInt();
    }

    /** Setzt den gespeicherten Betrag direkt (auf [0, capacity] begrenzt). */
    public void setEnergy(int amount) {
        set(Math.clamp(amount, 0, getCapacityAsInt()));
    }

    /** Maximales Entnahme-Limit pro Operation. */
    public int getMaxExtract() {
        return maxExtract;
    }
}
