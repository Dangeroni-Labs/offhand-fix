package dev.dangeroni.offhandfix.client;

import dev.dangeroni.offhandfix.OffhandFixConfig;
import dev.dangeroni.offhandfix.ShiftClickScope;
import java.util.EnumMap;
import java.util.Locale;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public final class OffhandFixConfigScreen extends Screen {
    private final Screen parent;
    private final EnumMap<ShiftClickScope, ButtonWidget> options = new EnumMap<>(ShiftClickScope.class);
    private boolean saveFailed;

    public OffhandFixConfigScreen(Screen parent) {
        super(Text.translatable("offhand_fix.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.options.clear();
        int center = this.width / 2;
        int top = Math.max(35, this.height / 2 - 77);
        int index = 0;
        for (ShiftClickScope scope : ShiftClickScope.values()) {
            ButtonWidget button = this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("offhand_fix.config.scope." + scope.name().toLowerCase(Locale.ROOT)),
                ignored -> this.select(scope)).dimensions(center - 100, top + 55 + index++ * 24, 200, 20).build());
            this.options.put(scope, button);
        }
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("offhand_fix.config.reset"),
            ignored -> this.select(ShiftClickScope.PLAYER_INVENTORY_ONLY))
            .dimensions(center - 100, top + 129, 200, 20).build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, ignored -> this.close())
            .dimensions(center - 100, top + 159, 200, 20).build());
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
    public void render(DrawContext graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int center = this.width / 2;
        int top = Math.max(35, this.height / 2 - 77);
        graphics.drawCenteredTextWithShadow(this.textRenderer, this.title, center, top, 0xFFFFFF);
        graphics.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("offhand_fix.config.scope"), center, top + 16, 0xFFFFFF);
        graphics.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("offhand_fix.config.description"), center, top + 29, 0xA0A0A0);
        graphics.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("offhand_fix.config.f_note"), center, top + 40, 0xA0A0A0);
        if (this.saveFailed) {
            graphics.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("offhand_fix.config.save_failed"), center, top + 181, 0xFF5555);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}
