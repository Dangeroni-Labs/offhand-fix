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

	public static boolean tryRefillOffhand(Player player, ItemStack sourceStack) {
		ItemStack offhandStack = player.getOffhandItem();
		if (!canRefillOffhand(offhandStack, sourceStack)) {
			return false;
		}

		int transferable = Math.min(sourceStack.getCount(), offhandStack.getMaxStackSize() - offhandStack.getCount());
		if (transferable <= 0) {
			return false;
		}

		offhandStack.grow(transferable);
		sourceStack.shrink(transferable);
		return true;
	}
}
