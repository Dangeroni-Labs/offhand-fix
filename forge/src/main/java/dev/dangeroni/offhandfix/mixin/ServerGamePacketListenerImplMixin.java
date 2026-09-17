package dev.dangeroni.offhandfix.mixin;

import dev.dangeroni.offhandfix.OffhandRefill;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class ServerGamePacketListenerImplMixin {
    @Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true)
    private void offhandFix$refillOffhandBeforeSwap(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        if (packet.getAction() != ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
            return;
        }

        ServerPlayer player = ((ServerGamePacketListenerImpl) (Object) this).getPlayer();
        ItemStack sourceStack = player.getMainHandItem();
        if (!OffhandRefill.tryRefillOffhand(player, sourceStack)) {
            return;
        }

        if (sourceStack.isEmpty()) {
            player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY);
        }

        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
        ci.cancel();
    }
}
