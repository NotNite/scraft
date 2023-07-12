package com.notnite.scraft.database

import com.notnite.scraft.Scraft
import com.notnite.scraft.generateKey
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import kotlin.collections.ArrayList

object ScraftDatabase {
    lateinit var db: Connection
    val migrations = arrayOf(
            """
            CREATE TABLE IF NOT EXISTS scraft_users (
                uuid TEXT NOT NULL PRIMARY KEY,
                username TEXT NOT NULL,
                api_key TEXT NOT NULL
            );
            """,
            "CREATE UNIQUE INDEX IF NOT EXISTS scraft_users__api_key ON scraft_users(api_key);",
            """
            CREATE TABLE IF NOT EXISTS scraft_bots (
                uuid TEXT NOT NULL PRIMARY KEY,
                owner TEXT NOT NULL,
                username TEXT NOT NULL,
                FOREIGN KEY(owner) REFERENCES scraft_users(uuid)
            );
            """,
            "CREATE UNIQUE INDEX IF NOT EXISTS scraft_bots__username ON scraft_bots(username);",
            """
            CREATE TABLE IF NOT EXISTS scraft_bots_all (
                uuid TEXT NOT NULL PRIMARY KEY,
                owner TEXT NOT NULL,
                username TEXT NOT NULL,
                FOREIGN KEY(owner) REFERENCES scraft_users(uuid)
            );
            """,
            "DELETE FROM scraft_bots_all; DELETE FROM scraft_bots;", // delete legacy bots
    )

    fun init() {
        db = DriverManager.getConnection("jdbc:sqlite:scraft.db")

        val stmt = db.createStatement()
        val rs = stmt.executeQuery("PRAGMA user_version")
        rs.next()

        val userVersion = rs.getInt(1)
        val sp = db.setSavepoint()

        try {
            for (i in (userVersion + 1)..migrations.size) {
                val migration = migrations[i - 1]
                stmt.executeUpdate(migration)
            }

            val ps = db.prepareStatement("PRAGMA user_version = ${migrations.size};")
            ps.executeUpdate()

            db.releaseSavepoint(sp)
        } catch (e: Exception) {
            Scraft.logger.error("Failed to migrate database", e)
            db.rollback(sp)
        }
    }

    fun validateAndFetchOwner(apiKey: String): ScraftUser? {
        val ps = db.prepareStatement("SELECT * FROM scraft_users WHERE api_key = ?;")
        ps.setString(1, apiKey)

        val rs = ps.executeQuery()
        if (!rs.next()) {
            ps.close()
            rs.close()
            return null
        }

        val o = ScraftUser(rs.getString("uuid"), rs.getString("api_key"), rs.getString("username"))
        ps.close()
        rs.close()
        return o
    }

    fun createOrUpdateUser(username: String, uuid: UUID) {
        val ps = db.prepareStatement("INSERT INTO scraft_users VALUES (?1, ?2, ?3) ON CONFLICT(uuid) DO UPDATE SET username = ?2, uuid = ?1")
        ps.setString(1, uuid.toString())
        ps.setString(2, username)
        ps.setString(3, generateKey())
        ps.executeUpdate()

        ps.close()
    }

    fun createBot(owner: UUID, username_: String): UUID? {
        val username = username_.lowercase()
        val uuid = UUID.nameUUIDFromBytes("scraft:$owner:$username".encodeToByteArray())
        val botExists = checkUsernameOwner(username)
        if (botExists != null) {
            return uuid
        }

        if (getClaimCount(owner) >= 10) {
            return null
        }

        val ps = db.prepareStatement("INSERT INTO scraft_bots VALUES (?1, ?2, ?3)")
        ps.setString(1, uuid.toString())
        ps.setString(2, owner.toString())
        ps.setString(3, username)
        ps.executeUpdate()

        val ps2 = db.prepareStatement("INSERT INTO scraft_bots_all VALUES (?1, ?2, ?3)")
        ps2.setString(1, uuid.toString())
        ps2.setString(2, owner.toString())
        ps2.setString(3, username)
        ps2.executeUpdate()

        ps.close()
        return uuid
    }

    fun deleteBot(owner: UUID, username: String) {
        val ps = db.prepareStatement("DELETE FROM scraft_bots WHERE owner = ? AND username = ?")
        ps.setString(1, owner.toString())
        ps.setString(2, username.lowercase())
        ps.executeUpdate()

        ps.close()
    }

    fun newAPIKey(uuid: UUID): String {
        val ps = db.prepareStatement("UPDATE scraft_users SET api_key = ? WHERE uuid = ?")
        val key = generateKey()
        ps.setString(1, key)
        ps.setString(2, uuid.toString())
        ps.executeUpdate()

        ps.close()
        return key
    }

    fun checkUsernameOwner(username: String): UUID? {
        val ps = db.prepareStatement("SELECT owner FROM scraft_bots WHERE username = ?")
        ps.setString(1, username.lowercase())
        val rs = ps.executeQuery()
        if (!rs.next()) {
            rs.close()
            ps.close()
            return null
        }

        val uuid = rs.getString("owner")
        return UUID.fromString(uuid)
    }

    fun isBotUUID(uuid: UUID): Boolean {
        val ps = db.prepareStatement("SELECT 1 FROM scraft_bots WHERE uuid = ?")
        ps.setString(1, uuid.toString())
        val rs = ps.executeQuery()
        val value = rs.next()

        rs.close()
        ps.close()
        return value
    }

    fun getClaimCount(user: UUID): Int {
        val ps = db.prepareStatement("SELECT count(*) AS count FROM scraft_bots WHERE owner = ?")
        ps.setString(1, user.toString())
        val rs = ps.executeQuery()
        rs.next()
        val value = rs.getInt("count")

        rs.close()
        ps.close()
        return value
    }

    fun getClaimedUsers(user: UUID): ArrayList<String> {
        val ps = db.prepareStatement("SELECT username FROM scraft_bots WHERE owner = ?")
        ps.setString(1, user.toString())
        val rs = ps.executeQuery()
        val out = ArrayList<String>()
        while (rs.next()) {
            out.add(rs.getString("username"))
        }

        rs.close()
        ps.close()
        return out
    }
}