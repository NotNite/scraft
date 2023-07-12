package com.notnite.scraft.mixin;

import com.notnite.scraft.Scraft;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerHandshakeNetworkHandler.class)
public class ServerHandshakeNetworkHandlerMixin {
    @Shadow
    @Final
    private ClientConnection connection;

    @Inject(method = "onHandshake", at = @At("HEAD"))
    private void onHandshake(HandshakeC2SPacket packet, CallbackInfo ci) {
        Scraft.INSTANCE.getConnectionState(this.connection).setHostname(packet.getAddress());
    }
}
