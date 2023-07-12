package com.notnite.scraft.database

import java.util.*

class ScraftUser(uuid: String, val apiKey: String, val username: String) {
    val uuid = UUID.fromString(uuid)
}
