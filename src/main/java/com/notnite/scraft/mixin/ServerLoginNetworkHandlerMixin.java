package com.notnite.scraft.mixin;

import com.mojang.authlib.GameProfile;
import com.notnite.scraft.Scraft;
import com.notnite.scraft.database.ScraftDatabase;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import org.apache.commons.lang3.Validate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin {
    @Shadow
    @Final
    MinecraftServer server;

    @Shadow
    GameProfile profile;

    @Shadow
    ServerLoginNetworkHandler.State state;

    @Shadow
    @Final
    ClientConnection connection;

    @Shadow
    @Final
    private byte[] nonce;

    /**
     * @author NotNite
     * @author Semisol
     * @reason nyaa~ :3
     */
    @Overwrite
    public void onHello(LoginHelloC2SPacket packet) {
        Validate.validState(this.state == ServerLoginNetworkHandler.State.HELLO, "Unexpected hello packet");
        Validate.validState(ServerLoginNetworkHandler.isValidName(packet.name()), "Invalid characters in username");
        this.profile = new GameProfile(null, packet.name());

        var state = Scraft.INSTANCE.getConnectionState(this.connection);
        var hostname = state.getHostname();
        Validate.validState(hostname != null, "Hostname is null");

        if (!hostname.startsWith("scraft_") && !hostname.startsWith("scraft-")) {
            // encrypted connection codepath stays for normal players
            this.state = ServerLoginNetworkHandler.State.KEY;
            this.connection.send(new LoginHelloS2CPacket("", this.server.getKeyPair().getPublic().getEncoded(), this.nonce));
        } else {
            state.setRealPlayer(false);
            // insert our logic here
            var self = (ServerLoginNetworkHandler) (Object) this;

            var split = hostname.split("\\.");
            var key = split[0].substring(7);

            var keyOwner = ScraftDatabase.INSTANCE.validateAndFetchOwner(key);
            if (keyOwner == null) {
                self.disconnect(Text.of("scraft: Invalid API key. Did you run /scraft apikey?"));
                return;
            }

            if (packet.name().length() > 15) {
                self.disconnect(Text.of("scraft: Name is too long (max 15 characters).\nDue to bots being prefixed with a *, bot names cannot be 16 characters."));
                return;
            }

            var nameOwner = ScraftDatabase.INSTANCE.checkUsernameOwner(packet.name());
            // if it's null we will get an NPE
            // unknown names are implicitly created
            if (nameOwner != null && !keyOwner.getUuid().equals(nameOwner)) {
                self.disconnect(Text.of("scraft: Username not owned by API key. Did you run /scraft claimuser?"));
                return;
            }

            var playerUUID = ScraftDatabase.INSTANCE.createBot(keyOwner.getUuid(), packet.name());
            if (playerUUID == null) {
                self.disconnect(Text.of("scraft: Couldn't create bot. Are you out of username claims?"));
                return;
            }

            // JUST TO PISS YOU OFF
            //this.connection.send(new LoginQueryRequestS2CPacket(0, new Identifier("scraft", "welcome"), PacketByteBufs.empty()));

            this.profile = new GameProfile(playerUUID, "+" + packet.name());
            this.state = ServerLoginNetworkHandler.State.READY_TO_ACCEPT;
        }
    }
}
