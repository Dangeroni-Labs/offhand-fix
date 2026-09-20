package dev.dangeroni.offhandfix;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class OffhandFixConfig {
    private static volatile ShiftClickScope shiftClickScope = ShiftClickScope.PLAYER_INVENTORY_ONLY;
    private static Path configFile;

    private OffhandFixConfig() {
    }

    public static ShiftClickScope shiftClickScope() {
        return shiftClickScope;
    }

    public static void load(Path configDirectory) {
        shiftClickScope = ShiftClickScope.PLAYER_INVENTORY_ONLY;
        Path file = configDirectory.resolve("offhand_fix.properties");
        configFile = file;
        Properties properties = new Properties();
        try {
            if (Files.exists(file)) {
                try (Reader reader = Files.newBufferedReader(file)) {
                    properties.load(reader);
                }
                shiftClickScope = ShiftClickScope.parse(properties.getProperty("shiftClickScope"));
            } else {
                Files.createDirectories(configDirectory);
                properties.setProperty("shiftClickScope", shiftClickScope.name());
                try (Writer writer = Files.newBufferedWriter(file)) {
                    properties.store(writer, "Shift-click refill: PLAYER_INVENTORY_ONLY, ALL_CONTAINERS, DISABLED.");
                }
            }
        } catch (IOException | IllegalArgumentException exception) {
            LogUtils.getLogger().warn("Unable to load {}; using PLAYER_INVENTORY_ONLY", file, exception);
        }
    }

    public static boolean setShiftClickScope(ShiftClickScope scope) {
        if (scope == null || configFile == null) {
            return false;
        }
        Properties properties = new Properties();
        try {
            if (Files.exists(configFile)) {
                try (Reader reader = Files.newBufferedReader(configFile)) {
                    properties.load(reader);
                }
            }
            properties.setProperty("shiftClickScope", scope.name());
            Files.createDirectories(configFile.getParent());
            try (Writer writer = Files.newBufferedWriter(configFile)) {
                properties.store(writer, "Shift-click refill: PLAYER_INVENTORY_ONLY, ALL_CONTAINERS, DISABLED.");
            }
            shiftClickScope = scope;
            return true;
        } catch (IOException | IllegalArgumentException exception) {
            LogUtils.getLogger().warn("Unable to save {}", configFile, exception);
            return false;
        }
    }
}
