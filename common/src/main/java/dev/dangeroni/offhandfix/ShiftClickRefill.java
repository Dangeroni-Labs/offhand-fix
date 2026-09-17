package dev.dangeroni.offhandfix;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;

public final class ShiftClickRefill {
    private ShiftClickRefill() {
    }

    public static boolean allowsSource(AbstractContainerMenu menu, Slot source, Player player) {
        if (source.getItem() == player.getOffhandItem()) {
            return false;
        }
        ShiftClickScope scope = OffhandFixConfig.shiftClickScope();
        if (scope == ShiftClickScope.DISABLED) {
            return false;
        }
        if (menu == player.inventoryMenu) {
            return !(source instanceof ResultSlot) || scope == ShiftClickScope.ALL_CONTAINERS;
        }
        // Player slots in an external menu express an outgoing transfer, even if
        // the menu later falls back to moving between inventory and hotbar.
        return scope == ShiftClickScope.ALL_CONTAINERS && source.container != player.getInventory();
    }

    public static boolean targetsPlayerInventory(AbstractContainerMenu menu, int start, int end, Player player) {
        if (start < 0 || end > menu.slots.size() || start >= end) {
            return false;
        }
        for (int index = start; index < end; index++) {
            if (menu.slots.get(index).container != player.getInventory()) {
                return false;
            }
        }
        return true;
    }
}
