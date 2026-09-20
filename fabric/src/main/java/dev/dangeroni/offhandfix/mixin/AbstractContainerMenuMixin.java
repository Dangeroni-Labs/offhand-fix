package dev.dangeroni.offhandfix.mixin;

import dev.dangeroni.offhandfix.OffhandRefill;
import dev.dangeroni.offhandfix.ShiftClickRefill;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenHandler.class)
abstract class AbstractContainerMenuMixin {
	@Shadow
	@Final
	public DefaultedList<Slot> slots;

	@Shadow
	public abstract void sendContentUpdates();

	@Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
	private void offhandfix$refillOffhandBeforeVanillaSlotAction(int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
		if (!(player instanceof ServerPlayerEntity) || slotId < 0 || slotId >= this.slots.size()) {
			return;
		}

		boolean offhandSwap = actionType == SlotActionType.SWAP && button == PlayerInventory.OFF_HAND_SLOT;
		if (!offhandSwap) {
			return;
		}

		Slot slot = this.slots.get(slotId);
		if (!slot.hasStack() || !slot.canTakeItems(player)) {
			return;
		}

		ItemStack sourceStack = slot.getStack();
		int transferred = OffhandRefill.refillOffhand(player.getOffHandStack(), sourceStack);
		if (transferred <= 0) {
			return;
		}

		if (sourceStack.isEmpty()) {
			slot.setStack(ItemStack.EMPTY);
			slot.onTakeItem(player, ItemStack.EMPTY);
		} else {
			slot.markDirty();
		}

		player.getInventory().markDirty();
		player.playerScreenHandler.sendContentUpdates();
		this.sendContentUpdates();
		ci.cancel();
	}

	@Unique
	private PlayerEntity offhandfix$quickMovePlayer;
	@Unique
	private boolean offhandfix$refilledTransfer;
	@Unique
	private boolean offhandfix$refilledClick;

	@Inject(method = "onSlotClick", at = @At("HEAD"))
	private void offhandfix$beginQuickMove(int slotId, int button, SlotActionType input, PlayerEntity player, CallbackInfo ci) {
		ScreenHandler menu = (ScreenHandler) (Object) this;
		this.offhandfix$quickMovePlayer = null;
		this.offhandfix$refilledClick = false;
		if (input == SlotActionType.QUICK_MOVE && (button == 0 || button == 1)
			&& slotId >= 0 && slotId < menu.slots.size()
			&& ShiftClickRefill.allowsSource(menu, menu.slots.get(slotId), player)) {
			this.offhandfix$quickMovePlayer = player;
		}
	}

	@Inject(method = "insertItem", at = @At("HEAD"))
	private void offhandfix$refillBeforeTransfer(ItemStack stack, int start, int end, boolean backwards, CallbackInfoReturnable<Boolean> ci) {
		this.offhandfix$refilledTransfer = false;
		PlayerEntity player = this.offhandfix$quickMovePlayer;
		ScreenHandler menu = (ScreenHandler) (Object) this;
		if (player != null && ShiftClickRefill.targetsPlayerInventory(menu, start, end, player)
			&& OffhandRefill.tryRefillOffhand(player, stack)) {
			this.offhandfix$refilledTransfer = true;
			this.offhandfix$refilledClick = true;
			player.getInventory().markDirty();
		}
	}

	@Inject(method = "insertItem", at = @At("RETURN"), cancellable = true)
	private void offhandfix$countOffhandTransfer(ItemStack stack, int start, int end, boolean backwards, CallbackInfoReturnable<Boolean> ci) {
		// Even if no remainder fits in inventory, vanilla must continue its
		// source-slot bookkeeping after a successful offhand transfer.
		if (this.offhandfix$refilledTransfer) {
			ci.setReturnValue(true);
		}
	}

	@Inject(method = "onSlotClick", at = @At("RETURN"))
	private void offhandfix$endQuickMove(int slotId, int button, SlotActionType input, PlayerEntity player, CallbackInfo ci) {
		this.offhandfix$quickMovePlayer = null;
		if (this.offhandfix$refilledClick && player instanceof ServerPlayerEntity) {
			player.playerScreenHandler.sendContentUpdates();
			((ScreenHandler) (Object) this).sendContentUpdates();
		}
		this.offhandfix$refilledClick = false;
	}
}
