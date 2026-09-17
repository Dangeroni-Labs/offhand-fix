package dev.dangeroni.offhandfix.mixin;

import dev.dangeroni.offhandfix.ScopeValidation;
import java.util.function.BooleanSupplier;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
abstract class ValidationServerMixin {
    @Unique
    private boolean offhandfix$validated;

    @Inject(method = "tickServer", at = @At("RETURN"), remap = false)
    private void offhandfix$validate(BooleanSupplier haveTime, CallbackInfo ci) {
        if (!this.offhandfix$validated) {
            this.offhandfix$validated = true;
            MinecraftServer server = (MinecraftServer) (Object) this;
            ScopeValidation.run(server);
            server.halt(false);
        }
    }
}
