# Dual-Dye Shulkers

[![Modrinth](https://img.shields.io/modrinth/dt/dual-dye-shulkers?logo=modrinth&label=Modrinth)](https://modrinth.com/project/dual-dye-shulkers)

A Fabric mod for Minecraft 1.21.1+ that lets you apply different colors to the **top (lid)** and **bottom (base)** of shulker boxes!

## Features

- **Dual-color shulker boxes** - Apply one color to the lid, another to the base
- **Configurable controls** (default):
  - Shift + right-click with dye → colors the **top (lid)**
  - Ctrl + right-click with dye → colors the **bottom (base)**
- **Works with all 16 dye colors** (256 combinations!)
- **Colors persist** through break/place cycles
- **Cauldron washing** - Remove custom colors with a water cauldron
- **Tooltip display** - See applied colors when hovering over items
- **Full 3D rendering** - Colors display correctly on placed blocks and items

## Usage

1. Place a shulker box
2. Hold any dye and Shift+right-click the shulker to color the **lid**
3. Hold any dye and Ctrl+right-click to color the **base**
4. To remove colors, use a water cauldron on the shulker box item

## Configuration

Install [Cloth Config](https://modrinth.com/mod/cloth-config) and [Mod Menu](https://modrinth.com/mod/modmenu) for an in-game config screen.

**Options:**
- **Show Color Tooltip** - Toggle tooltip display on shulker items
- **Dye Top (Lid)** - Customize key combo for coloring the top (default: Shift + Right Click)
- **Dye Bottom (Base)** - Customize key combo for coloring the bottom (default: Ctrl + Right Click)

Key combos can be any combination of two keys/mouse buttons.

## Requirements

- Minecraft 1.21.1 or 1.21.4 more versions comming soon!
- Fabric Loader 0.16.0+
- [Fabric API](https://modrinth.com/mod/fabric-api)

**Optional:**
- [Cloth Config](https://modrinth.com/mod/cloth-config) - For config screen
- [Mod Menu](https://modrinth.com/mod/modmenu) - For config screen access

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Download [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download this mod from [Modrinth](https://modrinth.com/project/dual-dye-shulkers)
4. Place both mods in your `mods` folder

## Building from Source

```bash
# Windows
gradlew.bat build

# Linux/macOS
./gradlew build
```

Output JAR will be in `build/libs/`

## License

MIT License - see [LICENSE](LICENSE) for details.
