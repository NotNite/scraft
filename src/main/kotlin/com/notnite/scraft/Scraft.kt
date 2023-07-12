package com.notnite.scraft

import com.notnite.scraft.commands.APIKeyCommand
import com.notnite.scraft.commands.ClaimUserCommand
import com.notnite.scraft.commands.DropUserCommand
import com.notnite.scraft.database.ScraftDatabase
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.network.ClientConnection
import net.minecraft.server.command.CommandManager
import org.slf4j.LoggerFactory

object Scraft : ModInitializer {
    val logger = LoggerFactory.getLogger("scraft")
    private val connections = mutableMapOf<ClientConnection, ScraftState>()

    override fun onInitialize() {
        ScraftDatabase.init()

        CommandRegistrationCallback.EVENT.register { dispatcher, registry, env ->
            if (!env.dedicated) return@register

            val branch = CommandManager.literal("scraft")

            ClaimUserCommand.register(branch)
            DropUserCommand.register(branch)
            APIKeyCommand.register(branch)

            dispatcher.register(branch)
        }
    }

    fun getConnectionState(connection: ClientConnection): ScraftState = connections.getOrPut(connection) { ScraftState() }
    fun removeConnectionState(connection: ClientConnection) = connections.remove(connection)
}
