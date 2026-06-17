package ch.danielt.akw.screen;

import ch.danielt.akw.AkwMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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

        // Kuehlbalken (blau) — 0..6 Kuehlrohre
        int cx = x + 7;
        int cBarTop = y + 17;
        int cBarH = 52;
        context.fill(cx, cBarTop, cx + 6, cBarTop + cBarH, 0xFF202020);
        int coolFill = (int) ((double) handler.getCoolingCount() / 6 * cBarH);
        context.fill(cx, cBarTop + (cBarH - coolFill), cx + 6, cBarTop + cBarH, 0xFF4488FF);
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

        int cx = x + 6;
        int cy = y + 16;
        if (mouseX >= cx && mouseX < cx + 8 && mouseY >= cy && mouseY < cy + 54) {
            int cooling = handler.getCoolingCount();
            int bonus = cooling * 15;
            context.drawTooltip(textRenderer,
                    Text.literal("Kühlung: " + cooling + "/6 Rohre (+" + bonus + "% FE/t)"),
                    mouseX, mouseY);
        }
    }
}
