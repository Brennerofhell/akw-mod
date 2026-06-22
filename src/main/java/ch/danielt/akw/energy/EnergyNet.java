package ch.danielt.akw.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * Gemeinsame FE-Verteil-Logik (NeoForge Transfer-API, 1.21.10). Wird von allen
 * energieabgebenden BlockEntities (Reaktor, Akku, Kabel) genutzt, damit die
 * Push-Logik nur an einer Stelle lebt — der modulare Kern des Energiesystems.
 */
public final class EnergyNet {

    private EnergyNet() {
    }

    /**
     * Versucht, FE aus {@code source} an alle sechs angrenzenden FE-Speicher
     * abzugeben (höchstens {@code limit} pro Seite und Tick).
     */
    public static void pushToNeighbors(EnergyHandler source, Level level, BlockPos pos, int limit) {
        if (source.getAmountAsInt() <= 0) {
            return;
        }
        for (Direction side : Direction.values()) {
            if (source.getAmountAsInt() <= 0) {
                break;
            }
            BlockPos neighborPos = pos.relative(side);
            EnergyHandler target = Capabilities.Energy.BLOCK.getCapability(
                    level, neighborPos, null, null, side.getOpposite());
            if (target == null) {
                continue;
            }
            try (Transaction tx = Transaction.openRoot()) {
                int moved = EnergyHandlerUtil.move(source, target, limit, tx);
                if (moved > 0) {
                    tx.commit();
                }
            }
        }
    }
}
