package dev.dangeroni.offhandfix;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;

public final class ShiftClickRefill {
    private ShiftClickRefill() {
    }

    public static boolean allowsSource(ScreenHandler menu, Slot source, PlayerEntity player) {
        if (source.getStack() == player.getOffHandStack()) {
            return false;
        }
        ShiftClickScope scope = OffhandFixConfig.shiftClickScope();
        if (scope == ShiftClickScope.DISABLED) {
            return false;
        }
        if (menu == player.playerScreenHandler) {
            return !(source instanceof CraftingResultSlot) || scope == ShiftClickScope.ALL_CONTAINERS;
        }
        return scope == ShiftClickScope.ALL_CONTAINERS && source.inventory != player.getInventory();
    }

    public static boolean targetsPlayerInventory(ScreenHandler menu, int start, int end, PlayerEntity player) {
        if (start < 0 || end > menu.slots.size() || start >= end) {
            return false;
        }
        for (int index = start; index < end; index++) {
            if (menu.slots.get(index).inventory != player.getInventory()) {
                return false;
            }
        }
        return true;
    }
}
