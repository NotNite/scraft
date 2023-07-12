package com.notnite.scraft.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.notnite.scraft.Scraft
import com.notnite.scraft.database.ScraftDatabase
import com.notnite.scraft.getState
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object DropUserCommand {
    fun register(root: LiteralArgumentBuilder<ServerCommandSource>) {
        val branch = CommandManager.literal("dropuser")
                .then(CommandManager.argument("name", StringArgumentType.string())
                        .executes {
                            try {
                                run(it)
                            } catch (e: Exception) {
                                Scraft.logger.error("Failed to run dropuser", e)
                                throw e
                            }
                        }
                )

        root.then(branch)
    }

    private fun run(context: CommandContext<ServerCommandSource>): Int {
        val state = getState(context)
        if (state == null) {
            context.source.sendError(Text.of("nice try"))
            return 0
        }

        val name = StringArgumentType.getString(context, "name")
        if (name.length > 15) {
            context.source.sendError(Text.of("Name is too long (max 15 characters)."))
            return 0
        }

        val owner = ScraftDatabase.checkUsernameOwner(name)
        if (owner != context.source.player!!.uuid) {
            context.source.sendError(Text.of("You don't own that username."))
            return 0
        }

        ScraftDatabase.deleteBot(
                context.source.player!!.uuid,
                name
        )

        val server = context.source.server
        if (server.playerManager.playerNames.contains("+$name")) {
            val player = server.playerManager.getPlayer("+$name")
            player?.networkHandler?.disconnect(Text.of("Your owner has slain you. Goodnight, sweet child."))
        }

        context.source.sendError(Text.of("Successfully dropped user."))
        return 1
    }
}
