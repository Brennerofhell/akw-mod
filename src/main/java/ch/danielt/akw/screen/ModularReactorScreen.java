package ch.danielt.akw.screen;

import ch.danielt.akw.AkwMod;
import ch.danielt.akw.block.entity.MultiblockReactorControllerBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Tab-GUI des Multiblock-Reaktors (176×222): Übersicht (Kennzahlen),
 * Steuerung (Modi, Ein/Aus, Abschalttemperatur, Steuerstab-Regler) und
 * Diagnose (Fehlerliste + Schichtansicht des Innenraums).
 */
public class ModularReactorScreen extends AbstractContainerScreen<ModularReactorScreenHandler> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AkwMod.MOD_ID, "textures/gui/modular_reactor.png");

    private enum Tab { OVERVIEW, CONTROL, DIAGNOSTICS }

    // Farbcodes der Schichtansicht (Index = CELL_*-Konstanten der Controller-BE)
    private static final int[] CELL_COLORS = {
            0xFF1E1E22,   // Luft
            0xFF8C8C94,   // Blei
            0xFFFFD83D,   // Kern
            0xFF4C7BFF,   // Steuerstab
            0xFF3DDCFF,   // Kühlrohr
            0xFFE03030,   // Fremdblock
    };
    private static final int GRID_X = 8;
    private static final int GRID_Y = 86;
    private static final int CELL_SIZE = 6;
    private static final int TEXT_COLOR = 0xFF404040;

    private Tab activeTab = Tab.OVERVIEW;
    private int layerIndex;

    private Button overviewTabButton;
    private Button controlTabButton;
    private Button diagnosticsTabButton;
    private Button redstoneButton;
    private Button comparatorButton;
    private Button enabledButton;
    private Button shutdownButton;
    private Button safetyButton;
    private RodSlider rodSlider;
    private Button layerUpButton;
    private Button layerDownButton;

    public ModularReactorScreen(ModularReactorScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        overviewTabButton = addRenderableWidget(Button.builder(
                Component.translatable("akw.gui.tab.overview"), btn -> switchTab(Tab.OVERVIEW))
                .bounds(leftPos, topPos - 22, 58, 20).build());
        controlTabButton = addRenderableWidget(Button.builder(
                Component.translatable("akw.gui.tab.control"), btn -> switchTab(Tab.CONTROL))
                .bounds(leftPos + 59, topPos - 22, 58, 20).build());
        diagnosticsTabButton = addRenderableWidget(Button.builder(
                Component.translatable("akw.gui.tab.diagnostics"), btn -> switchTab(Tab.DIAGNOSTICS))
                .bounds(leftPos + 118, topPos - 22, 58, 20).build());

        redstoneButton = addRenderableWidget(Button.builder(
                Component.translatable(menu.getRedstoneMode().translationKey()),
                btn -> sendButton(ModularReactorScreenHandler.BUTTON_REDSTONE))
                .bounds(leftPos + 8, topPos + 56, 76, 16).build());
        comparatorButton = addRenderableWidget(Button.builder(
                Component.translatable(menu.getComparatorMode().translationKey()),
                btn -> sendButton(ModularReactorScreenHandler.BUTTON_COMPARATOR))
                .bounds(leftPos + 92, topPos + 56, 76, 16).build());
        enabledButton = addRenderableWidget(Button.builder(
                enabledLabel(),
                btn -> sendButton(ModularReactorScreenHandler.BUTTON_ENABLED))
                .bounds(leftPos + 8, topPos + 74, 76, 16).build());
        shutdownButton = addRenderableWidget(Button.builder(
                shutdownLabel(),
                btn -> sendButton(ModularReactorScreenHandler.BUTTON_SHUTDOWN_TEMP))
                .bounds(leftPos + 92, topPos + 74, 76, 16).build());
        safetyButton = addRenderableWidget(Button.builder(
                safetyLabel(),
                btn -> sendButton(ModularReactorScreenHandler.BUTTON_SAFETY))
                .bounds(leftPos + 8, topPos + 92, 160, 16).build());
        rodSlider = addRenderableWidget(new RodSlider(
                leftPos + 8, topPos + 110, 160, 16, menu.getControlRodInsertion() / 100.0));

        layerUpButton = addRenderableWidget(Button.builder(Component.literal("▲"),
                btn -> changeLayer(1))
                .bounds(leftPos + 62, topPos + 86, 16, 14).build());
        layerDownButton = addRenderableWidget(Button.builder(Component.literal("▼"),
                btn -> changeLayer(-1))
                .bounds(leftPos + 62, topPos + 102, 16, 14).build());

        refreshTabWidgets();
    }

    private void switchTab(Tab tab) {
        activeTab = tab;
        refreshTabWidgets();
    }

    private void refreshTabWidgets() {
        overviewTabButton.active = activeTab != Tab.OVERVIEW;
        controlTabButton.active = activeTab != Tab.CONTROL;
        diagnosticsTabButton.active = activeTab != Tab.DIAGNOSTICS;

        boolean control = activeTab == Tab.CONTROL;
        redstoneButton.visible = control;
        comparatorButton.visible = control;
        enabledButton.visible = control;
        shutdownButton.visible = control;
        safetyButton.visible = control;
        rodSlider.visible = control;

        boolean diagnostics = activeTab == Tab.DIAGNOSTICS;
        layerUpButton.visible = diagnostics;
        layerDownButton.visible = diagnostics;
    }

    private void sendButton(int id) {
        Objects.requireNonNull(this.minecraft).gameMode
                .handleInventoryButtonClick(menu.containerId, id);
    }

    private void changeLayer(int delta) {
        int innerY = Math.max(1, menu.getSizeY() - 2);
        layerIndex = Math.clamp(layerIndex + delta, 0, innerY - 1);
    }

    private Component enabledLabel() {
        return Component.translatable(menu.isEnabled() ? "akw.gui.enabled.on" : "akw.gui.enabled.off");
    }

    private Component shutdownLabel() {
        return Component.translatable("akw.gui.shutdown_temp", menu.getShutdownTempPercent());
    }

    private Component safetyLabel() {
        return Component.translatable(menu.isSafetyOverride()
                ? "akw.gui.safety.off" : "akw.gui.safety.on");
    }

    @Nullable
    private MultiblockReactorControllerBlockEntity clientController() {
        if (this.minecraft == null || this.minecraft.level == null) {
            return null;
        }
        return this.minecraft.level.getBlockEntity(menu.getBlockPos())
                instanceof MultiblockReactorControllerBlockEntity be ? be : null;
    }

    /** Kompakte FE-Zahl (1.234 → „1,2k", 2.500.000 → „2,5M"). */
    private static String compact(int value) {
        if (value >= 1_000_000) {
            return String.format("%.1fM", value / 1_000_000.0);
        }
        if (value >= 10_000) {
            return String.format("%.0fk", value / 1_000.0);
        }
        if (value >= 1_000) {
            return String.format("%.1fk", value / 1_000.0);
        }
        return Integer.toString(value);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos,
                0.0F, 0.0F, imageWidth, imageHeight, 256, 256);

        // Energiebalken (grün) + Hitzebalken (orange/rot) — auf allen Tabs sichtbar
        int barTop = topPos + 17;
        int barH = 52;
        int barX = leftPos + 153;
        graphics.fill(barX, barTop, barX + 12, barTop + barH, 0xFF202020);
        int fillH = (int) (barH * menu.getEnergyFraction());
        graphics.fill(barX, barTop + (barH - fillH), barX + 12, barTop + barH, 0xFF3CC850);

        int heatX = leftPos + 137;
        graphics.fill(heatX, barTop, heatX + 12, barTop + barH, 0xFF202020);
        float heatFrac = menu.getHeatFraction();
        int heatFill = (int) (barH * heatFrac);
        int shutdownFrac = menu.getShutdownTempPercent();
        int heatColor = heatFrac * 100 >= shutdownFrac ? 0xFFE03030 : 0xFFE0902C;
        graphics.fill(heatX, barTop + (barH - heatFill), heatX + 12, barTop + barH, heatColor);

        // Brenn-Anzeige links neben dem Brennstoff-Slot
        int bx = leftPos + 72;
        int bTop = topPos + 34;
        int bH = 18;
        graphics.fill(bx, bTop, bx + 4, bTop + bH, 0xFF202020);
        int bf = (int) (bH * menu.getBurnFraction());
        graphics.fill(bx, bTop + (bH - bf), bx + 4, bTop + bH, 0xFFE0902C);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        switch (activeTab) {
            case OVERVIEW -> renderOverview(graphics);
            case CONTROL -> { }
            case DIAGNOSTICS -> renderDiagnostics(graphics);
        }
    }

    private void renderOverview(GuiGraphics graphics) {
        int y = 56;
        if (!menu.isAssembled()) {
            graphics.drawString(font, Component.translatable("akw.gui.status.unassembled"),
                    8, y, TEXT_COLOR, false);
            return;
        }
        graphics.drawString(font, Component.translatable("akw.gui.size",
                menu.getSizeX(), menu.getSizeY(), menu.getSizeZ()), 8, y, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("akw.gui.cores", menu.getCoreCount()),
                90, y, TEXT_COLOR, false);
        y += 12;
        graphics.drawString(font, Component.translatable("akw.gui.energy",
                compact(menu.getEnergy()), compact(menu.getCapacity())), 8, y, TEXT_COLOR, false);
        y += 12;
        graphics.drawString(font, Component.translatable("akw.gui.production",
                menu.getProductionPerTick()), 8, y, TEXT_COLOR, false);
        y += 12;
        graphics.drawString(font, Component.translatable("akw.gui.cooling",
                menu.getCoolingPerTick()), 8, y, TEXT_COLOR, false);
        y += 12;
        graphics.drawString(font, Component.translatable("akw.gui.heat",
                menu.getHeat(), menu.getMaxHeat()), 8, y, TEXT_COLOR, false);
        y += 12;
        graphics.drawString(font, Component.translatable("akw.gui.status",
                Component.translatable(menu.getStatus().translationKey())), 8, y, TEXT_COLOR, false);
    }

    private void renderDiagnostics(GuiGraphics graphics) {
        MultiblockReactorControllerBlockEntity controller = clientController();

        // Fehlerliste (max. 3 Zeilen, Rest wird gezählt)
        int y = 56;
        List<Component> errors = controller == null ? List.of() : controller.getLastErrorComponents();
        if (errors.isEmpty()) {
            graphics.drawString(font, Component.translatable("akw.gui.no_errors"), 8, y, 0xFF2E7D32, false);
        } else {
            int shown = Math.min(2, errors.size());
            for (int i = 0; i < shown; i++) {
                var lines = font.split(errors.get(i), 160);
                if (!lines.isEmpty()) {
                    graphics.drawString(font, lines.getFirst(), 8, y, 0xFFB02020, false);
                }
                y += 10;
            }
            if (errors.size() > shown) {
                graphics.drawString(font, Component.translatable("akw.gui.more_errors",
                        errors.size() - shown), 8, y, 0xFFB02020, false);
            }
        }

        // Schichtansicht
        if (controller == null || !menu.isAssembled()) {
            return;
        }
        int[] grid = controller.getClientInteriorGrid();
        int innerX = menu.getSizeX() - 2;
        int innerY = menu.getSizeY() - 2;
        int innerZ = menu.getSizeZ() - 2;
        if (grid == null || innerX <= 0 || grid.length != innerX * innerY * innerZ) {
            return;
        }
        layerIndex = Math.clamp(layerIndex, 0, innerY - 1);
        for (int z = 0; z < innerZ; z++) {
            for (int x = 0; x < innerX; x++) {
                int cell = grid[((layerIndex * innerZ) + z) * innerX + x];
                int color = cell >= 0 && cell < CELL_COLORS.length ? CELL_COLORS[cell] : CELL_COLORS[5];
                int px = GRID_X + x * CELL_SIZE;
                int py = GRID_Y + z * CELL_SIZE;
                graphics.fill(px, py, px + CELL_SIZE - 1, py + CELL_SIZE - 1, color);
            }
        }
        graphics.drawString(font, Component.translatable("akw.gui.layer",
                layerIndex + 1, innerY), 84, 90, TEXT_COLOR, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Labels/Widgets dynamisch aktualisieren (ContainerData wird jedes Frame gelesen)
        redstoneButton.setMessage(Component.translatable(menu.getRedstoneMode().translationKey()));
        comparatorButton.setMessage(Component.translatable(menu.getComparatorMode().translationKey()));
        enabledButton.setMessage(enabledLabel());
        shutdownButton.setMessage(shutdownLabel());
        safetyButton.setMessage(safetyLabel());
        rodSlider.syncFromServer();
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

    /** Stufenloser Steuerstab-Regler; sendet den Wert als Button-ID 100+n an den Server. */
    private class RodSlider extends AbstractSliderButton {

        RodSlider(int x, int y, int width, int height, double initial) {
            super(x, y, width, height, Component.empty(), initial);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("akw.gui.control_rods", (int) Math.round(value * 100)));
        }

        @Override
        protected void applyValue() {
            int percent = (int) Math.round(value * 100);
            if (percent != menu.getControlRodInsertion()) {
                sendButton(ModularReactorScreenHandler.BUTTON_ROD_BASE + percent);
            }
        }

        /**
         * Übernimmt den Serverwert, solange nicht gerade gezogen wird. Blockiert
         * NICHT auf isFocused() — Tastaturfokus bleibt oft auch nach dem Loslassen
         * bestehen und würde den Regler sonst dauerhaft vom Server-Sync abkoppeln.
         */
        void syncFromServer() {
            if (!dragging) {
                double serverValue = menu.getControlRodInsertion() / 100.0;
                if (Math.abs(serverValue - value) > 0.004) {
                    value = serverValue;
                    updateMessage();
                }
            }
        }
    }
}
