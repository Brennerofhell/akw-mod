package ch.danielt.akw.screen;

import ch.danielt.akw.AkwMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.Objects;

public class NuclearReactorScreen extends AbstractContainerScreen<NuclearReactorScreenHandler> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AkwMod.MOD_ID, "textures/gui/nuclear_reactor.png");

    private Button redstoneButton;
    private Button comparatorButton;

    public NuclearReactorScreen(NuclearReactorScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        redstoneButton = addRenderableWidget(Button.builder(
                Component.translatable(menu.getRedstoneMode().translationKey()),
                btn -> Objects.requireNonNull(this.minecraft).gameMode
                        .handleInventoryButtonClick(menu.containerId, 0))
                .bounds(leftPos + 7, topPos + 17, 71, 20)
                .build());
        comparatorButton = addRenderableWidget(Button.builder(
                Component.translatable(menu.getComparatorMode().translationKey()),
                btn -> Objects.requireNonNull(this.minecraft).gameMode
                        .handleInventoryButtonClick(menu.containerId, 1))
                .bounds(leftPos + 7, topPos + 40, 71, 20)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos,
                0.0F, 0.0F, imageWidth, imageHeight, 256, 256);

        // Energiebalken (gruen, von unten gefuellt)
        int barX = leftPos + 153;
        int barTop = topPos + 17;
        int barH = 52;
        int fillH = (int) (barH * menu.getEnergyFraction());
        graphics.fill(barX, barTop + (barH - fillH), barX + 12, barTop + barH, 0xFF3CC850);

        // Hitzebalken (links neben dem Energiebalken, von unten gefuellt)
        int heatX = leftPos + 137;
        graphics.fill(heatX, barTop, heatX + 12, barTop + barH, 0xFF202020);
        float heatFrac = menu.getHeatFraction();
        int heatFill = (int) (barH * heatFrac);
        // Orange im Normalbetrieb, Rot ab Drosselschwelle (75 %).
        int heatColor = heatFrac >= 0.75f ? 0xFFE03030 : 0xFFE0902C;
        graphics.fill(heatX, barTop + (barH - heatFill), heatX + 12, barTop + barH, heatColor);

        // Brenn-Anzeige (orange) links neben dem Brennstoff-Slot
        int bx = leftPos + 72;
        int bTop = topPos + 34;
        int bH = 18;
        graphics.fill(bx, bTop, bx + 4, bTop + bH, 0xFF202020);
        int bf = (int) (bH * menu.getBurnFraction());
        graphics.fill(bx, bTop + (bH - bf), bx + 4, bTop + bH, 0xFFE0902C);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Button-Labels dynamisch aktualisieren (ContainerData wird jedes Frame gelesen)
        redstoneButton.setMessage(Component.translatable(menu.getRedstoneMode().translationKey()));
        comparatorButton.setMessage(Component.translatable(menu.getComparatorMode().translationKey()));
        super.render(graphics, mouseX, mouseY, delta);

        int ex = leftPos + 152;
        int ey = topPos + 16;
        if (mouseX >= ex && mouseX < ex + 14 && mouseY >= ey && mouseY < ey + 54) {
            graphics.setTooltipForNextFrame(this.font,
                    Component.literal(menu.getEnergy() + " / " + menu.getCapacity() + " FE"),
                    mouseX, mouseY);
        }

        int hx = leftPos + 136;
        if (mouseX >= hx && mouseX < hx + 14 && mouseY >= ey && mouseY < ey + 54) {
            graphics.setTooltipForNextFrame(this.font,
                    Component.literal(menu.getHeat() + " / " + menu.getMaxHeat() + " Hitze"),
                    mouseX, mouseY);
        }
    }
}
