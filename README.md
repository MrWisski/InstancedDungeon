# InstancedDungeon
A Bukkit Plugin to provide instanced Dungeons
***
This is a WIP Bukkit plugin (and eventually, Sponge plugin) to create "Dungeons" (Bits of a map), and then replicate them on demand, giving a user their own area called an "Instance" (The realization of a dungeon in the world). When the player is done with their Instance, it is removed from the map.

By using its own custom WE Schematic format, schematics for the dungeon can be created on one server, and used on any other server that runs the mods used in the schematic, without ID errors.

Requirements :
1. WorldEdit (5.6.3).
1. AsynchWorldEdit (1.5).
1. MultiWorld (5.2.4-ish)
1. WorldGuard (5.9)
1. PowerNBT (7.3.1)

Pre-Alpha Feature List :
1. ~~Auto-creation of the iDungeon dimension~~
1. ~~Configuration misc.~~
1. ~~Programmatic cut, paste, copy from AsyncWorldEdit~~
1. ~~Custom Worldedit schematic format to save material names, instead of ID's, for cross-server compatibility~~
1. ~~Command structure~~
1. ~~Dungeon and Instance central management~~
1. ~~Dungeon and instance state based work-flow~~
1. ~~Instance owner tracking~~
1. ~~Player teleportation~~
1. ~~Basic dungeon editing with change tracking~~
1. ~~Basic dungeon prep~~
1. ~~Instance creation, clean up~~
1. Integrate with Thaumcraft (In Progress)
1. Dungeon prep - expand dungeon schematic -Y, add bedrock base layer)
1. Dungeon Edit command features (expand/contract dungeon boundary, mob spawns)
1. WorldGuard integration, protection of instances.
1. Index of created instances, regardless of success/failure, for cleanup.
1. DungeonSets - a group of similar dungeons (Say, the TF Hedge Maze), under a single name - when Instanced, a dungeon is randomly selected from the set, and used for the instance.

Beta-and-beyond Feature List (Beard's to-do List):
1. Instance Limits per day - Instances per day, per permission (for donators, special groups, events, etc).
1. Portals, or Portal signs for automatic creation of portals (at spawn)
1. A flag to save Entities with the instance (or perhaps add them during Edit?) - good for Villages
1. Instance TEAMS, integrated with WG.
1. Randomized loot spawns for chests.
1. Chop out the WE Schematic Format, and save it as its own plugin, because it's darn useful.
1. CommandHelper plugin to expose some of the ID internals to CH, allowing scripted encounters, statistics tracking, etc.
1. Triggers based on WG regions - player enter/exit region, trigger an event (boss spawn, and what not).

Instance-Compatible mods :
- Vanilla : Should be 100% compatible.
- Generic modded : Some NBT data doesn't carry over - yet.
- Twilight Forest : Tested extensively. Boss spawners carry over perfectly. Mob spawners carry over, but may be somewhat...hinky...with the non-tf dimension. More testing required.
- Thaumcraft : Needs some work, but doable by sidestepping thaumcrafts own internal maze registry.

