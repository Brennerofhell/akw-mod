package ch.danielt.akw.reactor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/** Einzelner Validierungsfehler der Multiblock-Hülle mit betroffener Position. */
public record ValidationError(Type type, BlockPos pos) {

    /** Übersetzte Meldung inklusive Koordinaten der betroffenen Position. */
    public Component toComponent() {
        return Component.translatable(type.translationKey(), pos.getX(), pos.getY(), pos.getZ());
    }

    public enum Type {
        GAP("akw.reactor.error.gap", true),
        FOREIGN_BLOCK("akw.reactor.error.foreign_block", true),
        NO_CORE("akw.reactor.error.no_core", true),
        NO_ENERGY_PORT("akw.reactor.error.no_energy_port", true),
        TOO_LARGE("akw.reactor.error.too_large", true),
        DISCONNECTED_PIPE("akw.reactor.error.disconnected_pipe", false);

        private final String translationKey;
        private final boolean blocksAssembly;

        Type(String translationKey, boolean blocksAssembly) {
            this.translationKey = translationKey;
            this.blocksAssembly = blocksAssembly;
        }

        public String translationKey() {
            return translationKey;
        }

        /** Nicht-blockierende Fehler (z. B. unverbundene Kühlrohre) verhindern die Assemblierung nicht. */
        public boolean blocksAssembly() {
            return blocksAssembly;
        }
    }
}
