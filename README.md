# Simple Orbital Strike Cannon

A Fabric mod that lets you call down orbital strikes with fishing rods. Supports Minecraft 1.21.2 through 1.21.11.

## How it works

Get a strike rod with `/orbitalstrike <type>` (requires op level 2), right-click anywhere, and a strike hits where you're looking after a 1 second delay. The rod breaks after one use.

Two strike types:
- `stab` - drops a full column of TNT from max height down to bedrock
- `nuke` - spawns expanding rings of TNT in a 60-block radius pattern above the target

## Commands

```
/orbitalstrike stab
/orbitalstrike nuke
/orbitalstrike stab 5
/orbitalstrike nuke 10
```

The optional number at the end lets you grab multiple rods at once (up to 64).

## Installation

1. Install Fabric Loader and Fabric API for your Minecraft version
2. Drop the mod jar in your `.minecraft/mods` folder

## Building

Requires Java 21.

```
gradlew.bat build
```

Output goes to `build/libs/`.

## License

MIT
