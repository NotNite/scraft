package com.notnite.scraft.mixin;

import com.notnite.scraft.Scraft;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
    This one goes out to the Mojang employee that wrote these lines of code:

    player.setGameMode(nbtCompound);
    ServerPlayNetworkHandler serverPlayNetworkHandler = new ServerPlayNetworkHandler(this.server, connection, player);

    I hope bad things happen to you.
 */

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        var state = Scraft.INSTANCE.getConnectionState(connection);
        if (state.isRealPlayer()) player.interactionManager.changeGameMode(GameMode.SPECTATOR);
    }
}
