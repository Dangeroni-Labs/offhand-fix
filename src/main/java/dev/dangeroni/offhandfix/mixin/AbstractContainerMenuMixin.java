package dev.dangeroni.offhandfix.mixin;
import dev.dangeroni.offhandfix.OffhandRefill;
import net.minecraft.entity.player.PlayerEntity;import net.minecraft.entity.player.PlayerInventory;import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;import net.minecraft.screen.slot.Slot;import net.minecraft.screen.slot.SlotActionType;import net.minecraft.server.network.ServerPlayerEntity;import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.Shadow;import org.spongepowered.asm.mixin.injection.At;import org.spongepowered.asm.mixin.injection.Inject;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ScreenHandler.class) abstract class AbstractContainerMenuMixin {
 @Shadow @Final public DefaultedList<Slot> slots; @Shadow public abstract void sendContentUpdates();
 @Inject(method="onSlotClick",at=@At("HEAD"),cancellable=true) private void offhandfix$refillOffhandBeforeVanillaSlotAction(int slotId,int button,SlotActionType actionType,PlayerEntity player,CallbackInfo ci){
  if(!(player instanceof ServerPlayerEntity)||slotId<0||slotId>=slots.size())return;if(actionType!=SlotActionType.QUICK_MOVE&&!(actionType==SlotActionType.SWAP&&button==PlayerInventory.OFF_HAND_SLOT))return;
  Slot slot=slots.get(slotId);if(!slot.hasStack()||!slot.canTakeItems(player))return;ItemStack source=slot.getStack();if(OffhandRefill.refillOffhand(player.getOffHandStack(),source)<=0)return;
  if(source.isEmpty()){slot.setStack(ItemStack.EMPTY);slot.onTakeItem(player,ItemStack.EMPTY);}else slot.markDirty();player.getInventory().markDirty();player.playerScreenHandler.sendContentUpdates();sendContentUpdates();ci.cancel();
 }
}
