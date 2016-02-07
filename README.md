# InstancedDungeon
A Bukkit Plugin to provide instanced Dungeons
***
This is a WIP Bukkit plugin (and eventually, Sponge plugin) to create "Dungeons" (Bits of a map), and then replicate them on demand, giving a user their own area called an "Instance" (The realization of a dungeon in the world). When the player is done with their Instance, it is removed from the map.

By using its own custom WE Schematic format, schematics for the dungeon can be created on one server, and used on any other server that runs the mods used in the schematic, without ID errors.

Requirements

- WorldEdit (5.6.3)
- AsynchWorldEdit (-5)
- MultiWorld (5.2.4-ish)
- WorldGuard (5.9)
- PowerNBT (7.3.1)

Pre-Alpha Feature List

- ~~Auto-creation of the iDungeon dimension~~
- ~~Configuration misc.~~
- ~~Programmatic cut, paste, copy from AsyncWorldEdit~~
- ~~Custom Worldedit schematic format to save material names, instead of ID's, for cross-server compatibility~~
- ~~Command structure~~
- ~~Dungeon and Instance central management~~
- ~~Dungeon and instance state based work-flow~~
- ~~Instance owner tracking~~
- ~~Player teleportation~~
- ~~Basic dungeon editing with change tracking~~
- ~~Basic dungeon prep~~
- ~~Instance creation, clean up~~
- Integrate with Thaumcraft (In Progress)
- Dungeon prep - expand dungeon schematic -Y, add bedrock base layer)
- Dungeon Edit command features (expand/contract dungeon boundary, mob spawns)
- WorldGuard integration, protection of instances.
- Index of created instances, regardless of success/failure, for cleanup.
- DungeonSets - a group of similar dungeons (Say, the TF Hedge Maze), under a single name - when Instanced, a dungeon is randomly selected from the set, and used for the instance.

Beta-and-beyond Feature List (Beard's to-do List)

- Instance Limits per day - Instances per day, per permission (for donators, special groups, events, etc)
- Portals, or Portal signs for automatic creation of portals (at spawn)
- A flag to save Entities with the instance (or perhaps add them during Edit?) - good for Villages
- Instance TEAMS, integrated with WG
- Randomized loot spawns for chests
- Chop out the WE Schematic Format, and save it as its own plugin, because it's darn useful
- CommandHelper plugin to expose some of the ID internals to CH, allowing scripted encounters, statistics tracking, etc
- Triggers based on WG regions - player enter/exit region, trigger an event (boss spawn, and what not)

Instance-Compatible mods :
- Vanilla : Should be 100% compatible
- Generic modded : Some NBT data doesn't carry over - yet
- Twilight Forest : Tested extensively. Boss spawners carry over perfectly. Mob spawners carry over, but may be somewhat...hinky...with the non-tf dimension. More testing required
- Thaumcraft : Needs some work, but doable by sidestepping thaumcrafts own internal maze registry
