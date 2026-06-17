package ch.danielt.akw.screen;

import ch.danielt.akw.AkwMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Client-seitiger GUI-Screen des Reaktors. Zeichnet den Hintergrund
 * ({@code textures/gui/nuclear_reactor.png}) und legt zwei dynamische Overlays
 * darüber: den von unten gefüllten grünen Energiebalken und die orange
 * Brenn-Anzeige. Alle angezeigten Werte stammen aus dem
 * {@link NuclearReactorScreenHandler} (gespeist vom serverseitig synchronisierten
 * {@code PropertyDelegate}) — der Screen hält keinen eigenen Zustand.
 *
 * <p>Beim Überfahren des Energiebalkens zeigt {@link #render} einen Tooltip
 * {@code <energie> / <kapazität> FE}.
 */
public class NuclearReactorScreen extends HandledScreen<NuclearReactorScreenHandler> {

    private static final Identifier TEXTURE =
            Identifier.of(AkwMod.MOD_ID, "textures/gui/nuclear_reactor.png");

    public NuclearReactorScreen(NuclearReactorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, x, y, 0f, 0f, backgroundWidth, backgroundHeight,
                backgroundWidth, backgroundHeight);

        // Energiebalken (gruen, von unten gefuellt)
        int barX = x + 153;
        int barTop = y + 17;
        int barH = 52;
        int fillH = (int) (barH * handler.getEnergyFraction());
        context.fill(barX, barTop + (barH - fillH), barX + 12, barTop + barH, 0xFF3CC850);

        // Brenn-Anzeige (orange) links neben dem Brennstoff-Slot
        int bx = x + 72;
        int bTop = y + 34;
        int bH = 18;
        context.fill(bx, bTop, bx + 4, bTop + bH, 0xFF202020);
        int bf = (int) (bH * handler.getBurnFraction());
        context.fill(bx, bTop + (bH - bf), bx + 4, bTop + bH, 0xFFE0902C);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        int ex = x + 152;
        int ey = y + 16;
        if (mouseX >= ex && mouseX < ex + 14 && mouseY >= ey && mouseY < ey + 54) {
            context.drawTooltip(textRenderer,
                    Text.literal(handler.getEnergy() + " / " + handler.getCapacity() + " FE"),
                    mouseX, mouseY);
        }
    }
}
