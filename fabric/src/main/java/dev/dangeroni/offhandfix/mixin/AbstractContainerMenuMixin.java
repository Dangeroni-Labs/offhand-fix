package dev.dangeroni.offhandfix.mixin;

import dev.dangeroni.offhandfix.OffhandRefill;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
abstract class AbstractContainerMenuMixin {
	@Shadow
	@Final
	public NonNullList<Slot> slots;

	@Shadow
	public abstract void broadcastChanges();

	@Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
	private void offhandfix$refillOffhandBeforeVanillaSlotAction(int slotId, int button, ContainerInput containerInput, Player player, CallbackInfo ci) {
		if (!(player instanceof ServerPlayer) || slotId < 0 || slotId >= this.slots.size()) {
			return;
		}

		boolean quickMove = containerInput == ContainerInput.QUICK_MOVE;
		boolean offhandSwap = containerInput == ContainerInput.SWAP && button == Inventory.SLOT_OFFHAND;
		if (!quickMove && !offhandSwap) {
			return;
		}

		Slot slot = this.slots.get(slotId);
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
		this.broadcastChanges();
		ci.cancel();
	}
}
