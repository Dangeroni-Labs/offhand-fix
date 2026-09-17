package dev.dangeroni.offhandfix.client;

import dev.dangeroni.offhandfix.OffhandFixConfig;
import dev.dangeroni.offhandfix.ShiftClickScope;
import java.util.EnumMap;
import java.util.Locale;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class OffhandFixConfigScreen extends Screen {
    private final Screen parent;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 33);
    private final EnumMap<ShiftClickScope, Button> options = new EnumMap<>(ShiftClickScope.class);
    private StringWidget status;

    public OffhandFixConfigScreen(Screen parent) {
        super(Component.translatable("offhand_fix.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.options.clear();
        this.layout.addTitleHeader(this.title, this.font);
        LinearLayout contents = this.layout.addToContents(LinearLayout.vertical().spacing(6));
        contents.defaultCellSetting().alignHorizontallyCenter();
        contents.addChild(new StringWidget(Component.translatable("offhand_fix.config.scope"), this.font));
        contents.addChild(new MultiLineTextWidget(Component.translatable("offhand_fix.config.description"), this.font)
            .setMaxWidth(Math.min(360, this.width - 32)).setCentered(true));
        contents.addChild(new StringWidget(Component.translatable("offhand_fix.config.f_note"), this.font));
        for (ShiftClickScope scope : ShiftClickScope.values()) {
            Button button = contents.addChild(Button.builder(Component.translatable(
                "offhand_fix.config.scope." + scope.name().toLowerCase(Locale.ROOT)), ignored -> this.select(scope)).width(200).build());
            this.options.put(scope, button);
        }
        contents.addChild(Button.builder(Component.translatable("offhand_fix.config.reset"),
            ignored -> this.select(ShiftClickScope.PLAYER_INVENTORY_ONLY)).width(200).build());
        this.status = contents.addChild(new StringWidget(Component.empty(), this.font));
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, ignored -> this.onClose()).width(200).build());
        this.layout.visitWidgets(this::addRenderableWidget);
        this.updateSelection();
        this.repositionElements();
    }

    private void select(ShiftClickScope scope) {
        boolean saved = OffhandFixConfig.setShiftClickScope(scope);
        this.status.setMessage(saved ? Component.empty() : Component.translatable("offhand_fix.config.save_failed"));
        this.updateSelection();
    }

    private void updateSelection() {
        this.options.forEach((scope, button) -> button.active = scope != OffhandFixConfig.shiftClickScope());
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
