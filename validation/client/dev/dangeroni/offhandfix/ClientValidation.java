package dev.dangeroni.offhandfix;

import dev.dangeroni.offhandfix.client.OffhandFixConfigScreen;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.KeyEvent;

public final class ClientValidation {
    private static int stage;
    private static int ticks;
    private static int checks;
    private static Screen parent;

    public static void tick(Minecraft minecraft) {
        try {
            if (stage == 0 && minecraft.gui.overlay() == null && minecraft.gui.screen() instanceof TitleScreen) {
                parent = minecraft.gui.screen();
                OffhandFixConfigScreen screen = integratedScreen(parent);
                minecraft.gui.setScreen(screen);
                List<Button> buttons = buttons(screen);
                check(buttons.size() == 5, "three scopes, reset and done");
                Path file = minecraft.gameDirectory.toPath().resolve("config/offhand_fix.properties");
                Files.writeString(file, Files.readString(file) + "\nunrelatedSetting=preserved\n");
                for (int index = 0; index < 3; index++) {
                    buttons.get(index).onPress(new KeyEvent(257, 0, 0));
                    check(OffhandFixConfig.shiftClickScope() == ShiftClickScope.values()[index], "immediate runtime selection");
                    for (int option = 0; option < 3; option++) check(buttons.get(option).active == (option != index), "selected button disabled");
                    check(Files.readString(file).contains("shiftClickScope=" + ShiftClickScope.values()[index]), "saved immediately");
                }
                buttons.get(3).onPress(new KeyEvent(257, 0, 0));
                check(OffhandFixConfig.shiftClickScope() == ShiftClickScope.PLAYER_INVENTORY_ONLY && !buttons.get(0).active, "reset default");
                check(Files.readString(file).contains("unrelatedSetting=preserved"), "reset preserves unrelated properties");
                buttons.get(4).onPress(new KeyEvent(257, 0, 0));
                check(minecraft.gui.screen() == parent, "done returns parent");
                screen = integratedScreen(parent);
                minecraft.gui.setScreen(screen);
                buttons(screen).get(1).onPress(new KeyEvent(257, 0, 0));
                screen.keyPressed(new KeyEvent(256, 0, 0));
                check(minecraft.gui.screen() == parent, "escape returns parent");
                OffhandFixConfig.load(file.getParent());
                check(OffhandFixConfig.shiftClickScope() == ShiftClickScope.ALL_CONTAINERS, "escape selection persists on reload");
                minecraft.gui.setScreen(integratedScreen(parent));
                stage = 1;
            } else if (stage == 1 && ++ticks > 20) {
                stage = 2;
                Screenshot.takeScreenshot(minecraft.gameRenderer.mainRenderTarget(), image -> {
                    try (image) {
                        image.writeToFile(minecraft.gameDirectory.toPath().resolve("config-screen.png"));
                        Files.writeString(minecraft.gameDirectory.toPath().resolve("gui-passed.txt"), "GUI VALIDATION PASS: " + checks + " assertions\n");
                        minecraft.execute(minecraft::stop);
                    } catch (Exception exception) {
                        throw new AssertionError(exception);
                    }
                });
            }
        } catch (Exception exception) {
            throw new AssertionError("GUI validation failed", exception);
        }
    }

    private static List<Button> buttons(Screen screen) {
        return screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
    }

    private static OffhandFixConfigScreen integratedScreen(Screen parent) throws Exception {
        // Reflection keeps this opt-in harness independent of loader classpaths.
        try {
            Class<?> modMenu = Class.forName("com.terraformersmc.modmenu.ModMenu");
            return (OffhandFixConfigScreen) modMenu.getMethod("getConfigScreen", String.class, Screen.class).invoke(null, "offhand_fix", parent);
        } catch (ClassNotFoundException ignored) {
        }
        try {
            Class<?> list = Class.forName("net.minecraftforge.fml.ModList");
            Object container = ((Optional<?>) list.getMethod("getModContainerById", String.class).invoke(null, "offhand_fix")).orElseThrow();
            Class<?> type = Class.forName("net.minecraftforge.client.ConfigScreenHandler$ConfigScreenFactory");
            Object factory = ((Optional<?>) container.getClass().getMethod("getCustomExtension", Class.class).invoke(container, type)).orElseThrow();
            BiFunction<Minecraft, Screen, Screen> function = (BiFunction<Minecraft, Screen, Screen>) type.getMethod("screenFunction").invoke(factory);
            return (OffhandFixConfigScreen) function.apply(Minecraft.getInstance(), parent);
        } catch (ClassNotFoundException ignored) {
        }
        try {
            Class<?> list = Class.forName("net.neoforged.fml.ModList");
            Object instance = list.getMethod("get").invoke(null);
            Object container = ((Optional<?>) list.getMethod("getModContainerById", String.class).invoke(instance, "offhand_fix")).orElseThrow();
            Class<?> type = Class.forName("net.neoforged.neoforge.client.gui.IConfigScreenFactory");
            Object factory = ((Optional<?>) container.getClass().getMethod("getCustomExtension", Class.class).invoke(container, type)).orElseThrow();
            return (OffhandFixConfigScreen) type.getMethod("createScreen", Class.forName("net.neoforged.fml.ModContainer"), Screen.class).invoke(factory, container, parent);
        } catch (ClassNotFoundException ignored) {
            return new OffhandFixConfigScreen(parent); // Fabric without optional Mod Menu.
        }
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
