package com.notnite.scraft.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.notnite.scraft.Scraft
import com.notnite.scraft.database.ScraftDatabase
import com.notnite.scraft.getState
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object ListUsersCommand {

    fun register(root: LiteralArgumentBuilder<ServerCommandSource>) {
        val branch = CommandManager.literal("listusers")
                .executes {
                    try {
                        run(it)
                    } catch (e: Exception) {
                        Scraft.logger.error("Failed to run listusers", e)
                        throw e
                    }
                }

        root.then(branch)
    }

    private fun run(context: CommandContext<ServerCommandSource>): Int {
        val state = getState(context)
        if (state == null) {
            context.source.sendError(Text.of("nice try"))
            return 0
        }

        val users = ScraftDatabase.getClaimedUsers(context.source.player!!.uuid)

        val text = if (users.size == 0) {
            Text.literal("You have no claimed usernames.").fillStyle(Style.EMPTY.withColor(Formatting.RED))
        } else {
            Text.literal("You have ${users.size} username claim${if (users.size == 1) "" else "s"}: ${users.joinToString(", ")}")
        }

        context.source.sendFeedback({
            text
        }, false)
        return 1
    }
}
