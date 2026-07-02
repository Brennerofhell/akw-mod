package ch.danielt.akw.reactor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Tests für {@link ReactorStatus#byOrdinal(int, ReactorStatus)} und translationKey(). */
class ReactorStatusTest {

    @ParameterizedTest(name = "{0}")
    @EnumSource(ReactorStatus.class)
    @DisplayName("byOrdinal() liefert für jeden gültigen Ordinal-Wert den passenden Status")
    void byOrdinalLiefertPassendenStatusBeiGueltigemOrdinal(ReactorStatus status) {
        assertSame(status, ReactorStatus.byOrdinal(status.ordinal(), ReactorStatus.OFFLINE));
    }

    @Test
    @DisplayName("byOrdinal() liefert den Fallback bei negativem Ordinal")
    void byOrdinalLiefertFallbackBeiNegativemOrdinal() {
        assertSame(ReactorStatus.DAMAGED, ReactorStatus.byOrdinal(-1, ReactorStatus.DAMAGED));
    }

    @Test
    @DisplayName("byOrdinal() liefert den Fallback bei zu großem Ordinal")
    void byOrdinalLiefertFallbackBeiZuGrossemOrdinal() {
        int zuGross = ReactorStatus.values().length;
        assertSame(ReactorStatus.OFFLINE, ReactorStatus.byOrdinal(zuGross, ReactorStatus.OFFLINE));
        assertSame(ReactorStatus.OFFLINE, ReactorStatus.byOrdinal(999, ReactorStatus.OFFLINE));
    }

    @Test
    @DisplayName("translationKey() ist der Statusname in Kleinbuchstaben mit Präfix")
    void translationKeyIstStatusnameInKleinbuchstaben() {
        assertEquals("akw.reactor.status.unassembled", ReactorStatus.UNASSEMBLED.translationKey());
        assertEquals("akw.reactor.status.running", ReactorStatus.RUNNING.translationKey());
        assertEquals("akw.reactor.status.scram", ReactorStatus.SCRAM.translationKey());
    }
}
