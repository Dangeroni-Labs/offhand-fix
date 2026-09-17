package dev.dangeroni.offhandfix.mixin;

import dev.dangeroni.offhandfix.OffhandRefill;
import dev.dangeroni.offhandfix.ShiftClickRefill;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerMenu.class)
abstract class AbstractContainerMenuMixin {
    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true, remap = false)
    private void offhandFix$refillOffhandBeforeVanillaSlotAction(int slotId, int button, ContainerInput containerInput, Player player, CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        if (!(player instanceof ServerPlayer) || slotId < 0 || slotId >= menu.slots.size()) {
            return;
        }

        boolean offhandSwap = containerInput == ContainerInput.SWAP && button == Inventory.SLOT_OFFHAND;
        if (!offhandSwap) {
            return;
        }

        Slot slot = menu.slots.get(slotId);
        if (!slot.hasItem() || !slot.mayPickup(player)) {
            return;
        }

        ItemStack sourceStack = slot.getItem();
        int transferred = OffhandRefill.refillOffhand(player.getOffhandItem(), sourceStack);
        if (transferred <= 0) {
            return;
        }

        if (sourceStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
            slot.onTake(player, ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        menu.broadcastChanges();
        ci.cancel();
    }

    @Unique
    private Player offhandfix$quickMovePlayer;
    @Unique
    private boolean offhandfix$refilledTransfer;
    @Unique
    private boolean offhandfix$refilledClick;

    @Inject(method = "clicked", at = @At("HEAD"), remap = false)
    private void offhandfix$beginQuickMove(int slotId, int button, ContainerInput input, Player player, CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        this.offhandfix$quickMovePlayer = null;
        this.offhandfix$refilledClick = false;
        if (input == ContainerInput.QUICK_MOVE && (button == 0 || button == 1)
            && slotId >= 0 && slotId < menu.slots.size()
            && ShiftClickRefill.allowsSource(menu, menu.slots.get(slotId), player)) {
            this.offhandfix$quickMovePlayer = player;
        }
    }

    @Inject(method = "moveItemStackTo", at = @At("HEAD"), remap = false)
    private void offhandfix$refillBeforeTransfer(ItemStack stack, int start, int end, boolean backwards, CallbackInfoReturnable<Boolean> ci) {
        this.offhandfix$refilledTransfer = false;
        Player player = this.offhandfix$quickMovePlayer;
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        if (player != null && ShiftClickRefill.targetsPlayerInventory(menu, start, end, player)
            && OffhandRefill.tryRefillOffhand(player, stack)) {
            this.offhandfix$refilledTransfer = true;
            this.offhandfix$refilledClick = true;
            player.getInventory().setChanged();
        }
    }

    @Inject(method = "moveItemStackTo", at = @At("RETURN"), cancellable = true, remap = false)
    private void offhandfix$countOffhandTransfer(ItemStack stack, int start, int end, boolean backwards, CallbackInfoReturnable<Boolean> ci) {
        // Even if no remainder fits in inventory, vanilla must continue its
        // source-slot bookkeeping after a successful offhand transfer.
        if (this.offhandfix$refilledTransfer) {
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "clicked", at = @At("RETURN"), remap = false)
    private void offhandfix$endQuickMove(int slotId, int button, ContainerInput input, Player player, CallbackInfo ci) {
        this.offhandfix$quickMovePlayer = null;
        if (this.offhandfix$refilledClick && player instanceof ServerPlayer) {
            player.inventoryMenu.broadcastChanges();
            ((AbstractContainerMenu) (Object) this).broadcastChanges();
        }
        this.offhandfix$refilledClick = false;
    }
}
