package dev.dangeroni.offhandfix;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class OffhandRefill {
	private OffhandRefill() {
	}

	public static boolean canRefillOffhand(ItemStack offhandStack, ItemStack sourceStack) {
		return !offhandStack.isEmpty()
			&& !sourceStack.isEmpty()
			&& offhandStack.getMaxStackSize() > 1
			&& ItemStack.isSameItemSameComponents(offhandStack, sourceStack);
	}

	public static int calculateTransferAmount(ItemStack offhandStack, ItemStack sourceStack) {
		if (!canRefillOffhand(offhandStack, sourceStack)) {
			return 0;
		}

		int space = offhandStack.getMaxStackSize() - offhandStack.getCount();
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

		offhandStack.grow(transferable);
		sourceStack.shrink(transferable);
		return transferable;
	}

	public static boolean tryRefillOffhand(Player player, ItemStack sourceStack) {
		return refillOffhand(player.getOffhandItem(), sourceStack) > 0;
	}
}
