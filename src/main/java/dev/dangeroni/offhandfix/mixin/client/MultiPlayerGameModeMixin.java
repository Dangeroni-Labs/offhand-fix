package dev.dangeroni.offhandfix.mixin.client;

import dev.dangeroni.offhandfix.OffhandRefill;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
abstract class MultiPlayerGameModeMixin {
    @Shadow(remap = false)
    @Final
    private ClientPacketListener connection;

    @Inject(method = "handleInventoryMouseClick", at = @At("HEAD"), cancellable = true, remap = false)
    private void offhandFix$mirrorQuickMoveRefillLocally(int containerId, int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (clickType != ClickType.QUICK_MOVE) {
            return;
        }

        AbstractContainerMenu menu = player.containerMenu;
        if (containerId != menu.containerId || slotId < 0 || slotId >= menu.slots.size()) {
            return;
        }

        Slot slot = menu.slots.get(slotId);
        if (!slot.hasItem()) {
            return;
        }

        ItemStack sourceStack = slot.getItem();
        ItemStack offhandStack = player.getOffhandItem();
        if (OffhandRefill.calculateTransferAmount(offhandStack, sourceStack) <= 0) {
            return;
        }

        List<ItemStack> previousStacks = this.offhandFix$snapshotStacks(menu);
        OffhandRefill.refillOffhand(offhandStack, sourceStack);

        if (sourceStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        player.getInventory().setChanged();

        Int2ObjectMap<ItemStack> changedStacks = this.offhandFix$collectChangedStacks(menu, previousStacks);
        this.connection.send(new ServerboundContainerClickPacket(
            containerId,
            menu.getStateId(),
            slotId,
            button,
            clickType,
            menu.getCarried().copy(),
            changedStacks
        ));
        ci.cancel();
    }

    private List<ItemStack> offhandFix$snapshotStacks(AbstractContainerMenu menu) {
        List<ItemStack> snapshot = new ArrayList<>(menu.slots.size());
        for (Slot slot : menu.slots) {
            snapshot.add(slot.getItem().copy());
        }
        return snapshot;
    }

    private Int2ObjectMap<ItemStack> offhandFix$collectChangedStacks(AbstractContainerMenu menu, List<ItemStack> previousStacks) {
        Int2ObjectMap<ItemStack> changes = new Int2ObjectOpenHashMap<>();
        for (int index = 0; index < menu.slots.size(); index++) {
            ItemStack previous = previousStacks.get(index);
            ItemStack current = menu.slots.get(index).getItem();
            if (!ItemStack.matches(previous, current)) {
                changes.put(index, current.copy());
            }
        }
        return changes;
    }
}
