package com.notnite.scraft

import com.notnite.scraft.database.ScraftUser

class ScraftState {
    var isRealPlayer = true
    var hostname: String? = null
    var owner: ScraftUser? = null
}
