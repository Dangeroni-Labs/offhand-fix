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
			&& offhandStack.getCount() < offhandStack.getMaxStackSize()
			&& ItemStack.isSameItemSameComponents(offhandStack, sourceStack);
	}

	public static int refillOffhand(ItemStack offhandStack, ItemStack sourceStack) {
		if (!canRefillOffhand(offhandStack, sourceStack)) {
			return 0;
		}

		int transferable = Math.min(sourceStack.getCount(), offhandStack.getMaxStackSize() - offhandStack.getCount());
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
