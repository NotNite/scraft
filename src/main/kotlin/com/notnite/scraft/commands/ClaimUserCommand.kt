package com.notnite.scraft.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.notnite.scraft.Scraft
import com.notnite.scraft.database.ScraftDatabase
import com.notnite.scraft.getState
import com.notnite.scraft.mixin.ServerPlayNetworkHandlerAccessor
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerLoginNetworkHandler
import net.minecraft.text.Text

object ClaimUserCommand {
    fun register(root: LiteralArgumentBuilder<ServerCommandSource>) {
        val branch = CommandManager.literal("claimuser")
                .then(CommandManager.argument("name", StringArgumentType.string())
                        .executes { run(it) }
                )

        root.then(branch)
    }

    fun run(context: CommandContext<ServerCommandSource>): Int {
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

        if (!ServerLoginNetworkHandler.isValidName(name)) {
            context.source.sendError(Text.of("Username is not valid (disallowed characters?)."))
            return 0
        }

        val taken = ScraftDatabase.checkUsernameOwner(name) != null
        if (taken) {
            context.source.sendError(Text.of("Username is already taken."))
            return 0
        }

        val worked = ScraftDatabase.createBot(context.source.player!!.uuid, name) != null
        return if (worked) {
            context.source.sendFeedback({
                Text.of("Successfully claimed username $name.")
            }, false)
            1
        } else {
            context.source.sendFeedback({
                Text.of("Couldn't claim username - you might be out of bot slots.")
            }, false)
            0
        }
    }
}
