Run the opt-in Minecraft 26.2 dedicated-server suite:

```sh
./gradlew -I validation/server-tests.gradle :fabric:runServer :forge:runServer :neoforge:runServer --no-parallel
```

Each loader uses an isolated `build/scope-validation` world and must log
`SCOPE VALIDATION PASS: 213 assertions`. The suite exercises actual menu clicks
under every scope: standalone inventory, both chest/furnace directions,
crafting-table player slots, seven-item results, partial capacity, repeated
crafting, ingredient consumption, bottle remainders, crafting statistics,
and gameplay/hovered-slot F refill and vanilla fallback. Invalid config values
and malformed properties also fall back safely.

The script adds validation sources and a test-only mixin to development runs.
After using it, run `./gradlew clean buildAll` without the init script before
using release jars. Normal project scripts do not include validation sources.

Manual client checks remain necessary on all three loaders: matching client/server
settings, smooth prediction and synchronization (including multiplayer latency),
full player inventory during partial crafting, standalone 2x2 crafting, barrel,
hopper, modded menus, and generic Mouse Tweaks interactions. Also check recipe
advancement notifications and loader-specific crafting hooks with other mods.
The config is local and is not automatically synchronized to clients.
