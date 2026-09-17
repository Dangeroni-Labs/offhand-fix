# Kossman's Offhand Fix

Kossman's Offhand Fix improves how matching item stacks interact with the offhand.

## Features

- **Shift-click** compatible stacks in the player inventory to refill the offhand first
- Press `F` to refill a matching offhand stack before swapping
- Works with inventory slots and normal gameplay
- Preserves vanilla behavior when refilling is not possible
- Server-authoritative and multiplayer-safe

## Configuration

Edit `config/offhand_fix.properties` and restart. `shiftClickScope` accepts:

- `PLAYER_INVENTORY_ONLY` - Player Inventory Only (default): refill only in the standalone inventory; crafting results remain vanilla.
- `ALL_CONTAINERS` - All Containers: also refill incoming container stacks and crafting results; player-to-container transfers remain vanilla.
- `DISABLED` - Disabled: all Shift-click transfers remain vanilla.

This setting does not affect `F`. On multiplayer servers the server setting controls behavior; use the same setting on clients for matching prediction.

## Supported versions

| Minecraft | Fabric | NeoForge | Forge |
| --- | --- | --- | --- |
| 26.2 | Yes | Yes | Yes |
| 1.20.1 | Yes | - | Yes |

## Downloads

- [GitHub Releases](https://github.com/Dangeroni-Labs/offhand-fix/releases)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/kossmans-offhand-fix)

## Website

https://dangeroni-labs.vercel.app/mods/kossman-offhand-fix

## License

[MIT](LICENSE)
