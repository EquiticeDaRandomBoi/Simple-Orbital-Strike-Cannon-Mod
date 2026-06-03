# Simple Orbital Strike Cannon

A Fabric mod that lets you call down orbital strikes with fishing rods. Supports 1.21 through 1.21.11 and 26.1, 26.1.1, 26.1.2.

## How it works

Get a strike rod with `/orbitalstrike <type>` (requires op level 2), right-click anywhere, and a strike hits where you're looking after a 1 second delay. The rod breaks after one use.

Two strike types:
- `stab` - drops a full column of TNT from max height down to bedrock
- `nuke` - spawns expanding rings of TNT in a big radius above the target and scatters them outward

## Commands

```
/orbitalstrike stab
/orbitalstrike nuke
/orbitalstrike stab 5
/orbitalstrike nuke 10
```

The optional number lets you grab multiple rods at once (up to 64).

## Installation

1. Install Fabric Loader and Fabric API for your Minecraft version
2. Drop the mod jar in your `.minecraft/mods` folder

Works on dedicated servers and in singleplayer.

## Building

Requires Java 21 for 1.21.x, Java 25 for 26.1.x.

```
gradlew.bat build
```

Output goes to `build/libs/`.

## License

MIT
