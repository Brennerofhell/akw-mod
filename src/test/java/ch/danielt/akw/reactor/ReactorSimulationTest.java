package ch.danielt.akw.reactor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.danielt.akw.reactor.ReactorSimulation.ReactorStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Reine Rechentests für {@link ReactorSimulation} — keine Weltabhängigkeit. */
class ReactorSimulationTest {

    /** Baut eine {@link ReactorLayout} mit sinnvollen Default-Kontaktwerten (0). */
    private static ReactorLayout layout(int coreCount, int coreNeighborContacts,
            int coreControlRodContacts, int coreCoolingContacts) {
        return new ReactorLayout(
                0, 0, 0,
                3, 3, 3,
                coreCount,
                0, // controlRodCount
                0, // coolingPipeCount
                0, // connectedCoolingPipeCount
                coreNeighborContacts,
                coreControlRodContacts,
                coreCoolingContacts,
                1, // energyPortCount
                0  // itemPortCount
        );
    }

    @Test
    @DisplayName("Einzelner Kern ohne Kühlung: nur passive Kühlung, positive Generation/Wärme")
    void einzelnerKernOhneKuehlungHatNurPassiveKuehlung() {
        ReactorLayout single = layout(1, 0, 0, 0);

        ReactorStats stats = ReactorSimulation.calculate(single, 1, 0);

        assertEquals(48, stats.generationPerTick());
        assertEquals(4, stats.heatPerTick());
        assertEquals(ReactorSimulation.PASSIVE_COOLING, stats.coolingPerTick());
        assertEquals(100_000, stats.capacity());
        assertEquals(1_800, stats.maxHeat());
    }

    @Test
    @DisplayName("Gekühlter Vierkernreaktor: Kühlkontakte erhöhen die Kühlung deutlich über den Passivwert")
    void gekuehlterVierkernreaktorHatHoehereKuehlung() {
        ReactorLayout uncooled = layout(4, 0, 0, 0);
        ReactorLayout cooled = layout(4, 0, 0, 8);

        ReactorStats uncooledStats = ReactorSimulation.calculate(uncooled, 4, 0);
        ReactorStats cooledStats = ReactorSimulation.calculate(cooled, 4, 0);

        assertEquals(ReactorSimulation.PASSIVE_COOLING, uncooledStats.coolingPerTick());
        assertEquals(66, cooledStats.coolingPerTick());
        assertTrue(cooledStats.coolingPerTick() > uncooledStats.coolingPerTick());
        // Kühlung beeinflusst weder Generation noch Wärmeerzeugung.
        assertEquals(uncooledStats.generationPerTick(), cooledStats.generationPerTick());
        assertEquals(uncooledStats.heatPerTick(), cooledStats.heatPerTick());
    }

    @Test
    @DisplayName("Steuerstab-Wärmereduktion (diskret über Kontakte): mehr Kontakte senken heatPerTick, Generation bleibt gleich")
    void steuerstabKontakteSenkenWaermeAberNichtGeneration() {
        ReactorLayout ohneStaebe = layout(4, 0, 0, 0);
        ReactorLayout einStabProKern = layout(4, 0, 4, 0);
        ReactorLayout zweiStaebeProKern = layout(4, 0, 8, 0);
        ReactorLayout ueberKappung = layout(4, 0, 100, 0);

        ReactorStats ohne = ReactorSimulation.calculate(ohneStaebe, 4, 0);
        ReactorStats einer = ReactorSimulation.calculate(einStabProKern, 4, 0);
        ReactorStats zwei = ReactorSimulation.calculate(zweiStaebeProKern, 4, 0);
        ReactorStats gekappt = ReactorSimulation.calculate(ueberKappung, 4, 0);

        assertEquals(17, ohne.heatPerTick());
        assertEquals(13, einer.heatPerTick());
        assertEquals(9, zwei.heatPerTick());
        // controlRodsPerCore ist auf 2.0 gedeckelt -> identisches Ergebnis wie bei genau 2 Kontakten/Kern.
        assertEquals(zwei.heatPerTick(), gekappt.heatPerTick());

        // Generation hängt nur von der Reaktivität ab, nicht von den Steuerstab-Kontakten.
        assertEquals(192, ohne.generationPerTick());
        assertEquals(192, einer.generationPerTick());
        assertEquals(192, zwei.generationPerTick());
        assertEquals(192, gekappt.generationPerTick());
    }

    @Test
    @DisplayName("Steuerstab-Einschub (stufenlos): 0/50/100 % skalieren Generation und Wärme, 100 % schaltet beides ab")
    void steuerstabEinschubSkaliertStufenlos() {
        ReactorLayout single = layout(1, 0, 0, 0);

        ReactorStats bei0 = ReactorSimulation.calculate(single, 1, 0);
        ReactorStats bei50 = ReactorSimulation.calculate(single, 1, 50);
        ReactorStats bei100 = ReactorSimulation.calculate(single, 1, 100);

        assertEquals(48, bei0.generationPerTick());
        assertEquals(4, bei0.heatPerTick());

        assertEquals(24, bei50.generationPerTick());
        assertEquals(1, bei50.heatPerTick());

        assertEquals(0, bei100.generationPerTick());
        assertEquals(0, bei100.heatPerTick());
    }

    @Test
    @DisplayName("Teilbetrieb: activeCores < coreCount begrenzt die Generation proportional")
    void teilbetriebBegrenztGeneration() {
        ReactorLayout vierKerne = layout(4, 0, 0, 0);

        ReactorStats volllast = ReactorSimulation.calculate(vierKerne, 4, 0);
        ReactorStats halblast = ReactorSimulation.calculate(vierKerne, 2, 0);

        assertEquals(192, volllast.generationPerTick());
        assertEquals(96, halblast.generationPerTick());
        assertEquals(9, halblast.heatPerTick());
    }

    @Test
    @DisplayName("activeCores über coreCount wird auf coreCount begrenzt (kein Überlauf der Generation)")
    void activeCoresUeberCoreCountWirdGeklemmt() {
        ReactorLayout vierKerne = layout(4, 0, 0, 0);

        ReactorStats normal = ReactorSimulation.calculate(vierKerne, 4, 0);
        ReactorStats ueberhoeht = ReactorSimulation.calculate(vierKerne, 10, 0);

        assertEquals(normal.generationPerTick(), ueberhoeht.generationPerTick());
        assertEquals(normal.heatPerTick(), ueberhoeht.heatPerTick());
    }

    @Test
    @DisplayName("Kapazität: max(100_000, coreCount × 100_000)")
    void kapazitaetSkaliertMitKernzahl() {
        assertEquals(100_000, ReactorSimulation.capacity(layout(0, 0, 0, 0)));
        assertEquals(100_000, ReactorSimulation.capacity(layout(1, 0, 0, 0)));
        assertEquals(500_000, ReactorSimulation.capacity(layout(5, 0, 0, 0)));
    }

    @Test
    @DisplayName("Maximaltemperatur: 1600 + max(1, coreCount) × 200")
    void maxHeatSkaliertMitKernzahl() {
        assertEquals(1_800, ReactorSimulation.maxHeat(layout(0, 0, 0, 0)));
        assertEquals(1_800, ReactorSimulation.maxHeat(layout(1, 0, 0, 0)));
        assertEquals(2_600, ReactorSimulation.maxHeat(layout(5, 0, 0, 0)));
    }

    @Test
    @DisplayName("ReactorLayout.EMPTY liefert keine Generation und nur passive Kühlung")
    void emptyLayoutLiefertNullwerte() {
        ReactorStats stats = ReactorSimulation.calculate(ReactorLayout.EMPTY, 1, 0);

        assertEquals(0, stats.generationPerTick());
        assertEquals(0, stats.heatPerTick());
        assertEquals(ReactorSimulation.PASSIVE_COOLING, stats.coolingPerTick());
        assertEquals(100_000, stats.capacity());
        assertEquals(1_800, stats.maxHeat());
    }

    @Test
    @DisplayName("activeCores <= 0 liefert trotz assemblierter Hülle keine Generation")
    void keineAktivenKerneLiefernNullwerte() {
        ReactorLayout vierKerne = layout(4, 0, 0, 0);

        ReactorStats nullAktiv = ReactorSimulation.calculate(vierKerne, 0, 0);
        ReactorStats negativAktiv = ReactorSimulation.calculate(vierKerne, -5, 0);

        assertEquals(0, nullAktiv.generationPerTick());
        assertEquals(0, nullAktiv.heatPerTick());
        assertEquals(ReactorSimulation.PASSIVE_COOLING, nullAktiv.coolingPerTick());
        assertEquals(400_000, nullAktiv.capacity());
        assertEquals(2_400, nullAktiv.maxHeat());

        assertEquals(0, negativAktiv.generationPerTick());
        assertEquals(ReactorSimulation.PASSIVE_COOLING, negativAktiv.coolingPerTick());
    }

    @Test
    @DisplayName("Einschub-Werte außerhalb 0–100 werden geklemmt statt Fehler zu werfen")
    void einschubWirdGeklemmt() {
        ReactorLayout single = layout(1, 0, 0, 0);

        ReactorStats negativ = ReactorSimulation.calculate(single, 1, -20);
        ReactorStats beiNull = ReactorSimulation.calculate(single, 1, 0);
        assertEquals(beiNull.generationPerTick(), negativ.generationPerTick());
        assertEquals(beiNull.heatPerTick(), negativ.heatPerTick());

        ReactorStats ueber100 = ReactorSimulation.calculate(single, 1, 150);
        assertEquals(0, ueber100.generationPerTick());
        assertEquals(0, ueber100.heatPerTick());
    }

    @Test
    @DisplayName("Ergebnisse sind niemals negativ, auch bei extremen Eingaben")
    void ergebnisseSindNieNegativ() {
        ReactorLayout extrem = layout(1, 1000, 1000, 1000);

        for (int insertion : new int[] {-500, 0, 25, 50, 75, 100, 500}) {
            ReactorStats stats = ReactorSimulation.calculate(extrem, 1, insertion);
            assertTrue(stats.generationPerTick() >= 0, "generation < 0 bei insertion=" + insertion);
            assertTrue(stats.heatPerTick() >= 0, "heat < 0 bei insertion=" + insertion);
            assertTrue(stats.coolingPerTick() >= 0, "cooling < 0 bei insertion=" + insertion);
            assertTrue(stats.capacity() >= 0, "capacity < 0 bei insertion=" + insertion);
            assertTrue(stats.maxHeat() >= 0, "maxHeat < 0 bei insertion=" + insertion);
        }
    }

    @Test
    @DisplayName("Reaktivität ist auf 1.0 gedeckelt: mehr als 4 Nachbarkontakte/Kern erhöhen die Generation nicht weiter")
    void reaktivitaetIstGedeckelt() {
        ReactorLayout single = layout(1, 4, 0, 0); // baseReactivity bereits bei 1.0
        ReactorLayout extrem = layout(1, 1000, 0, 0); // würde ohne Deckel weit über 1.0 liegen

        ReactorStats stats = ReactorSimulation.calculate(single, 1, 0);
        ReactorStats extremStats = ReactorSimulation.calculate(extrem, 1, 0);

        assertEquals(ReactorSimulation.FE_PER_CORE, stats.generationPerTick());
        assertEquals(stats.generationPerTick(), extremStats.generationPerTick());
    }
}
