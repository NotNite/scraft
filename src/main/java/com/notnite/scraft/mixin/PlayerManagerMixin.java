package com.notnite.scraft.mixin;

import com.notnite.scraft.Scraft;
import com.notnite.scraft.database.ScraftDatabase;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        var connectionState = Scraft.INSTANCE.getConnectionState(connection);
        var isRealPlayer = connectionState.isRealPlayer();

        if (isRealPlayer) {
            ScraftDatabase.INSTANCE.createOrUpdateUser(player.getName().getString(), player.getUuid());
        }
    }
}
