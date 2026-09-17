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
Scope changes use the backend setter without reloading, checking that the next
menu click uses the new value immediately.

The script adds validation sources and a test-only mixin to development runs.
After using it, run `./gradlew clean buildAll` without the init script before
using release jars. Normal project scripts do not include validation sources.

Manual client checks remain necessary on all three loaders: matching client/server
settings, smooth prediction and synchronization (including multiplayer latency),
full player inventory during partial crafting, standalone 2x2 crafting, barrel,
hopper, modded menus, and generic Mouse Tweaks interactions. Also check recipe
advancement notifications and loader-specific crafting hooks with other mods.
The config is local and is not automatically synchronized to clients.

GUI checks (real clients, isolated `build/gui-validation` directories):

```sh
./gradlew -I validation/client-tests.gradle :fabric:runClient
./gradlew -I validation/client-tests.gradle -PwithModMenu :fabric:runClient :forge:runClient :neoforge:runClient --no-parallel
```

These check the registered screen factories (when present), each selection,
disabled selected state, immediate persistence/application, reset, unrelated
properties, Done/Escape parent navigation, and reload persistence. Each client
writes `gui-passed.txt` and `config-screen.png`. The first command excludes
optional Mod Menu; `-PwithModMenu` only adds it to the Fabric development runtime.
Run `./gradlew clean buildAll` afterwards to remove all test-only classes.
Manually check the Mods-list Config button, different GUI scales, actual restart
persistence, and changing the scope while an integrated-server world is open.
