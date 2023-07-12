# Scraft

Scraft is a Minecraft mod inspired by [Screeps](https://screeps.com) that turns your server into a bot programming environment. Users log in and generate API keys, which allows them to log in as bot users in their code. Only bots are allowed on the server - human players are placed in spectator mode to observe the bots' actions. (While there is nothing stopping a user from implementing the login flow on a normal Minecraft client and playing normally, it is discouraged.)

## Usage

This mod works best with the following configurations:

- Disable chat signing with `enforce-secure-profile=false` in your server.properties.
- Enable a whitelist.
- Setting up a domain with [wildcard DNS](https://en.wikipedia.org/wiki/Wildcard_DNS_record), which is supported by providers like Cloudflare. Make two DNS records, `scraft.example.com` and `*.scraft.example.com`, pointing to the same server.

Whitelisted players can join the server and will be placed in spectator mode. They can then run `/scraft apikey` to generate an API key.

To join as a bot, connect to the domain `scraft_(api key).scraft.example.com`, or you can fabricate the hostname in the handshake packet if you wish. No authentication is required - you can pick any username, so long as it is not claimed by another user.

Bot names will be claimed automatically on join, or you can run `/scraft claimuser` to claim a username. One user can hold up to 10 bot names, and bot names can be released with the `/scraft dropuser` command. Bots will have their names prefixed with the `+` character to indicate they are bots.

If you remove a user from the whitelist, you will need to remove their API key and bot as well (currently only possible through manual database editing).
