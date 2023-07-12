package com.notnite.scraft.mixin;

import com.notnite.scraft.Scraft;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(method = "handleDisconnection", at = @At("TAIL"))
    private void handleDisconnection(CallbackInfo ci) {
        var self = (ClientConnection) (Object) this;
        Scraft.INSTANCE.removeConnectionState(self);
    }
}
