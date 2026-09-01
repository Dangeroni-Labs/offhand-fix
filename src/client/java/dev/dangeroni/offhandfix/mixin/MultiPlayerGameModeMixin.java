package dev.dangeroni.offhandfix.mixin;

import dev.dangeroni.offhandfix.OffhandRefill;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
abstract class MultiPlayerGameModeMixin {
	@Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
	private void offhandfix$mirrorQuickMoveRefillLocally(
		int syncId,
		int slotId,
		int button,
		SlotActionType actionType,
		PlayerEntity player,
		CallbackInfo ci
	) {
		if (actionType != SlotActionType.QUICK_MOVE) {
			return;
		}

		ScreenHandler menu = player.currentScreenHandler;
		if (syncId != menu.syncId || slotId < 0 || slotId >= menu.slots.size()) {
			return;
		}

		Slot slot = menu.getSlot(slotId);
		if (!slot.hasStack()) {
			return;
		}

		ItemStack sourceStack = slot.getStack();
		ItemStack offhandStack = player.getOffHandStack();
		if (OffhandRefill.calculateTransferAmount(offhandStack, sourceStack) <= 0) {
			return;
		}

		ClientPlayNetworkHandler connection = MinecraftClient.getInstance().getNetworkHandler();
		if (connection == null) {
			return;
		}

		List<ItemStack> previousStacks = snapshotStacks(menu);
		OffhandRefill.refillOffhand(offhandStack, sourceStack);

		if (sourceStack.isEmpty()) {
			slot.setStack(ItemStack.EMPTY);
		} else {
			slot.markDirty();
		}

		player.getInventory().markDirty();

		Int2ObjectMap<ItemStack> changedStacks = collectChangedStacks(menu, previousStacks);
		connection.sendPacket(new ClickSlotC2SPacket(
			syncId,
			menu.getRevision(),
			(short) slotId,
			(byte) button,
			actionType,
			menu.getCursorStack().copy(),
			changedStacks
		));
		ci.cancel();
	}

	private List<ItemStack> snapshotStacks(ScreenHandler menu) {
		List<ItemStack> snapshot = new ArrayList<>(menu.slots.size());
		for (Slot slot : menu.slots) {
			snapshot.add(slot.getStack().copy());
		}

		return snapshot;
	}

	private Int2ObjectMap<ItemStack> collectChangedStacks(ScreenHandler menu, List<ItemStack> previousStacks) {
		Int2ObjectMap<ItemStack> changes = new Int2ObjectOpenHashMap<>();
		for (int index = 0; index < menu.slots.size(); index++) {
			ItemStack previous = previousStacks.get(index);
			ItemStack current = menu.getSlot(index).getStack();
			if (!ItemStack.areEqual(previous, current)) {
				changes.put(index, current.copy());
			}
		}

		return changes;
	}
}
