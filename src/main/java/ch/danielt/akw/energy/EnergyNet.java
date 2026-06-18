package ch.danielt.akw.energy;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;
import team.reborn.energy.api.base.SimpleEnergyStorage;

/**
 * Gemeinsame FE-Verteil-Logik (Team Reborn Energy). Wird von allen
 * energieabgebenden BlockEntities (Reaktor, Akku, Kabel) genutzt, damit die
 * Push-Logik nur an einer Stelle lebt — der modulare Kern des Energiesystems.
 */
public final class EnergyNet {

    private EnergyNet() {
    }

    /**
     * Versucht, FE aus {@code source} an alle sechs angrenzenden FE-Speicher
     * abzugeben (höchstens {@code maxPerSide} pro Seite und Tick).
     */
    public static void pushToNeighbors(SimpleEnergyStorage source, World world, BlockPos pos, long maxPerSide) {
        if (source.amount <= 0) {
            return;
        }
        for (Direction dir : Direction.values()) {
            EnergyStorage target = EnergyStorage.SIDED.find(world, pos.offset(dir), dir.getOpposite());
            if (target == null) {
                continue;
            }
            try (Transaction tx = Transaction.openOuter()) {
                EnergyStorageUtil.move(source, target, maxPerSide, tx);
                tx.commit();
            }
        }
    }
}
