package dev.dangeroni.offhandfix.mixin;

import dev.dangeroni.offhandfix.ClientValidation;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class ValidationClientMixin {
    @Inject(method = "tick", at = @At("RETURN"), remap = false)
    private void offhandfix$validateGui(CallbackInfo ci) {
        ClientValidation.tick((Minecraft) (Object) this);
    }
}
