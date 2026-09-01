package dev.dangeroni.offhandfix;

import java.util.Objects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public final class OffhandRefill {
	private OffhandRefill() {
	}

	public static boolean canRefillOffhand(ItemStack offhandStack, ItemStack sourceStack) {
		return !offhandStack.isEmpty()
			&& !sourceStack.isEmpty()
			&& offhandStack.getMaxCount() > 1
			&& ItemStack.areItemsEqual(offhandStack, sourceStack)
			&& Objects.equals(offhandStack.getNbt(), sourceStack.getNbt());
	}

	public static int calculateTransferAmount(ItemStack offhandStack, ItemStack sourceStack) {
		if (!canRefillOffhand(offhandStack, sourceStack)) {
			return 0;
		}

		int space = offhandStack.getMaxCount() - offhandStack.getCount();
		if (space <= 0) {
			return 0;
		}

		int transferable = Math.min(sourceStack.getCount(), space);
		if (transferable <= 0) {
			return 0;
		}

		return transferable;
	}

	public static int refillOffhand(ItemStack offhandStack, ItemStack sourceStack) {
		int transferable = calculateTransferAmount(offhandStack, sourceStack);
		if (transferable <= 0) {
			return 0;
		}

		offhandStack.increment(transferable);
		sourceStack.decrement(transferable);
		return transferable;
	}

	public static boolean tryRefillOffhand(PlayerEntity player, ItemStack sourceStack) {
		return refillOffhand(player.getOffHandStack(), sourceStack) > 0;
	}
}
