package dev.dangeroni.offhandfix.client;

import dev.dangeroni.offhandfix.OffhandFixConfig;
import dev.dangeroni.offhandfix.ShiftClickScope;
import java.util.EnumMap;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class OffhandFixConfigScreen extends Screen {
    private final Screen parent;
    private final EnumMap<ShiftClickScope, Button> options = new EnumMap<>(ShiftClickScope.class);
    private boolean saveFailed;

    public OffhandFixConfigScreen(Screen parent) {
        super(Component.translatable("offhand_fix.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.options.clear();
        int center = this.width / 2;
        int top = Math.max(35, this.height / 2 - 77);
        int index = 0;
        for (ShiftClickScope scope : ShiftClickScope.values()) {
            Button button = this.addRenderableWidget(Button.builder(
                Component.translatable("offhand_fix.config.scope." + scope.name().toLowerCase(Locale.ROOT)),
                ignored -> this.select(scope)).bounds(center - 100, top + 55 + index++ * 24, 200, 20).build());
            this.options.put(scope, button);
        }
        this.addRenderableWidget(Button.builder(Component.translatable("offhand_fix.config.reset"),
            ignored -> this.select(ShiftClickScope.PLAYER_INVENTORY_ONLY))
            .bounds(center - 100, top + 129, 200, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, ignored -> this.onClose())
            .bounds(center - 100, top + 159, 200, 20).build());
        this.updateSelection();
    }

    private void select(ShiftClickScope scope) {
        this.saveFailed = !OffhandFixConfig.setShiftClickScope(scope);
        this.updateSelection();
    }

    private void updateSelection() {
        this.options.forEach((scope, button) -> button.active = scope != OffhandFixConfig.shiftClickScope());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int center = this.width / 2;
        int top = Math.max(35, this.height / 2 - 77);
        graphics.drawCenteredString(this.font, this.title, center, top, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("offhand_fix.config.scope"), center, top + 16, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("offhand_fix.config.description"), center, top + 29, 0xA0A0A0);
        graphics.drawCenteredString(this.font, Component.translatable("offhand_fix.config.f_note"), center, top + 40, 0xA0A0A0);
        if (this.saveFailed) {
            graphics.drawCenteredString(this.font, Component.translatable("offhand_fix.config.save_failed"), center, top + 181, 0xFF5555);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
