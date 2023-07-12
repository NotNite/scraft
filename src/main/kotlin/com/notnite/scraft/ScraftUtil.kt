package com.notnite.scraft

import com.mojang.brigadier.context.CommandContext
import com.notnite.scraft.mixin.ServerPlayNetworkHandlerAccessor
import net.minecraft.server.command.ServerCommandSource
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.*

val SECURE_RANDOM = SecureRandom()

fun generateKey(): String = UUID.randomUUID().toString()

fun getState(context: CommandContext<ServerCommandSource>): ScraftState? {
    val player = context.source.player ?: return null
    val networkHandler = player.networkHandler as ServerPlayNetworkHandlerAccessor
    val connection = networkHandler.getConnection()
    val state = Scraft.getConnectionState(connection)

    if (!state.isRealPlayer) return null
    return state
}

fun uuidFromBytes(data: ByteArray): UUID {
    val bb = ByteBuffer.wrap(data)
    return UUID(bb.getLong(), bb.getLong())
}

fun randomUUID(version: Int): UUID {
    val randomBytes = ByteArray(16)
    SECURE_RANDOM.nextBytes(randomBytes)
    randomBytes[6] = (randomBytes[6].toInt() and 0x0f).toByte() /* clear version        */
    randomBytes[6] = (randomBytes[6].toInt() or (version shl 4)).toByte()  /* set to version 3     */
    randomBytes[8] = (randomBytes[8].toInt() and 0x3f).toByte() /* clear variant        */
    randomBytes[8] = (randomBytes[8].toInt() or 0x80).toByte()  /* set to IETF variant  */
    return uuidFromBytes(randomBytes)
}
