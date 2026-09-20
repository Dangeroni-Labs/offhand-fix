# Kossman's Offhand Fix

Kossman's Offhand Fix improves how matching item stacks interact with the offhand.

## Features

- **Shift-click** compatible stacks in the player inventory to refill the offhand first
- Press `F` to refill a matching offhand stack before swapping
- Works with inventory slots and normal gameplay
- Preserves vanilla behavior when refilling is not possible
- Server-authoritative and multiplayer-safe

## Configuration

Open Mods -> Kossman's Offhand Fix -> Config. On Fabric, install the optional Mod Menu mod to show this button. Changes and Reset apply immediately; Done and Escape return to the previous screen. Manual edits to `config/offhand_fix.properties` require a restart.

`shiftClickScope` accepts:

- `PLAYER_INVENTORY_ONLY` (default): refill only in the standalone player inventory; crafting results remain vanilla.
- `ALL_CONTAINERS`: also refill incoming container stacks and crafting results; player-to-container transfers remain vanilla.
- `DISABLED`: all Shift-click transfers remain vanilla.

This setting does not affect `F`.

## Supported versions

| Minecraft | Fabric | NeoForge | Forge |
| --- | --- | --- | --- |
| 26.2 | Yes | Yes | Yes |
| 1.20.1 | Yes | Yes | Yes |

## Downloads

- [GitHub Releases](https://github.com/Dangeroni-Labs/offhand-fix/releases)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/kossmans-offhand-fix)

## Website

https://dangeroni-labs.vercel.app/mods/kossman-offhand-fix

## License

[MIT](LICENSE)
