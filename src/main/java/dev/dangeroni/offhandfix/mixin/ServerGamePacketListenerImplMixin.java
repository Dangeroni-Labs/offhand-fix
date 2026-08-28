package dev.dangeroni.offhandfix.mixin;

import dev.dangeroni.offhandfix.OffhandRefill;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class ServerGamePacketListenerImplMixin {
    @Shadow(remap = false)
    @Final
    private ServerPlayer player;

    @Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true, remap = false)
    private void offhandFix$refillOffhandBeforeSwap(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        if (packet.getAction() != ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
            return;
        }

        ItemStack sourceStack = this.player.getMainHandItem();
        if (!OffhandRefill.tryRefillOffhand(this.player, sourceStack)) {
            return;
        }

        if (sourceStack.isEmpty()) {
            this.player.getInventory().setSelectedItem(ItemStack.EMPTY);
        }

        this.player.getInventory().setChanged();
        this.player.containerMenu.broadcastChanges();
        ci.cancel();
    }
}
