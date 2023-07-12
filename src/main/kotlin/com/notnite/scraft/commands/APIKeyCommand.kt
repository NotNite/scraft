package com.notnite.scraft.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.notnite.scraft.Scraft
import com.notnite.scraft.database.ScraftDatabase
import com.notnite.scraft.getState
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object APIKeyCommand {
    fun register(root: LiteralArgumentBuilder<ServerCommandSource>) {
        val branch = CommandManager.literal("apikey")
                .executes {
                    try {
                        run(it)
                    } catch (e: Exception) {
                        Scraft.logger.error("fuck", e)
                        return@executes 0
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

        val key = ScraftDatabase.newAPIKey(context.source.player!!.uuid)

        val obfuscatedKeyText = Text.literal("is_anyone_even_reading_this_lmfao_nuts")
                .fillStyle(Style.EMPTY
                        .withFormatting(Formatting.OBFUSCATED)
                        .withClickEvent(ClickEvent(
                                ClickEvent.Action.COPY_TO_CLIPBOARD,
                                key
                        ))
                        .withHoverEvent(HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Text.literal("Click to copy.")
                        ))
                )

        val text = Text.literal("Your API key is: ")
                .append(obfuscatedKeyText)
                .append(Text.literal(". Don't share this with anyone else!"))

        context.source.sendFeedback({
            text
        }, false)
        return 1
    }
}
