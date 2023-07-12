package com.notnite.scraft.mixin;

import com.mojang.authlib.GameProfile;
import com.notnite.scraft.database.ScraftDatabase;
import net.minecraft.server.dedicated.DedicatedPlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedPlayerManager.class)
public class DedicatedPlayerManagerMixin {
   @Inject(method = "isWhitelisted", at = @At("HEAD"), cancellable = true)
    private void isWhitelisted(GameProfile profile, CallbackInfoReturnable<Boolean> cir) {
       var uuid = profile.getId();
       if (ScraftDatabase.INSTANCE.isBotUUID(uuid)) cir.setReturnValue(true);
   }
}
