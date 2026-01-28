# Multi-Color Shulker Boxes

A Fabric mod for Minecraft 1.21.4 that allows you to apply multiple colors to shulker boxes!

## Features

- **Multi-color shulker boxes**: Apply different colors to the top and bottom of shulker boxes
- **Simple interaction**: Right-click shulker boxes with dyes to color them
- **Visual feedback**: Tooltips show which colors are applied
- **Survival-friendly**: Works in both creative and survival modes

## Usage

### Applying Colors

1. **Place a shulker box** in the world (any color works)
2. **Color the top**: Right-click the shulker box with any dye
3. **Color the bottom**: Sneak (shift) + right-click with any dye
4. **Pick up the box**: Break it to pick it up - the colors are saved!

### Checking Colors

Hover over a colored shulker box in your inventory to see a tooltip showing which colors are applied:
```
Custom Colors:
  Top: Red
  Bottom: Blue
```

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.4
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download the mod JAR from [Releases](../../releases)
4. Place it in your `.minecraft/mods` folder
5. Launch Minecraft with the Fabric profile

## Building from Source

### Prerequisites

- Java 21 or higher

### Build

```bash
# Windows
gradlew.bat build

# Mac/Linux
./gradlew build
```

The compiled JAR will be in `build/libs/`

## Compatibility

- **Minecraft**: 1.21.4
- **Fabric Loader**: 0.16.0+
- **Fabric API**: Required

## License

MIT License - see [LICENSE](LICENSE) for details.
