<h1 align="center">
  <div style="
    display: inline-block;
    width: 32px;
    height: 32px;
    background-image: url('https://raw.githubusercontent.com/Omicron-Industries/PhoenixWiki/master/src/main/resources/assets/phoenix_wiki/textures/items/phoenix_feather.png');
    background-position: 0 0;
    background-repeat: no-repeat;
    image-rendering: pixelated;
    transform: scale(8.75);
    transform-origin: center top;
    margin-bottom: 240px;
  "></div>
</h1>

<p align="center">
  <strong>The theme of your own life can be as fleeting as a rose. But it can also be just as pretty.<br>
    Learning about yourself causes you to spark and flume.</strong>
</p>

<p align="center">
  <a href="https://www.curseforge.com/minecraft/mc-mods/phoenixwiki">
    <img alt="CurseForge" height="50" src="https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/cozy/available/curseforge_vector.svg"></a>
  <a href="https://discord.gg/4jch9Rs2Cq">
    <img alt="Discord" height="50" src="https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/cozy/social/discord-singular_vector.svg"></a>
  <a href="https://ko-fi.com/phoenixvine">
    <img alt="Ko-fi" height="50" src="https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/cozy/donate/kofi-singular_vector.svg"></a>
</p>

# What is Phoenix Wiki?
Wiki is a small mod designed to be used within the PhoenixSuite as a library mod.
It handles themeing, the Markdown parsing, and the ingame wiki.  It is meant to be JarinJar included in your mod.

Contributions are highly welcome!

## Wiki link
We have a small in progress wiki for all PhoenixSuite mods, if it is missing any important info or you would like to help,
feel free to ping me on discord by the username of Phoenixvine.

[Wiki](https://omicron-industries.github.io/PhoenixSuite/wiki/)

# Getting started as a dev
If you are a dev wanting to use Wiki in your mod, you will need to:
1. Depend on the mod using either cursemaven or the repsy repository.
```gradle
    // Add the following to your mavens section
    repositories {
        maven {
            url "https://cursemaven.com/"
            content {
                includeGroup "curse.maven"
            }
        }
        
        maven {
            name = "Repsy"
            url = uri("https://repo.repsy.io/mvn/user75142941/phoenixsuite")
        }
    }
    
    dependencies {
        modImplementation("net.phoenixvine.wiki:phoenix_wiki:0.2.8")
    }
```

## 1. Using the In-Game Wiki
The easiest way to provide documentation is to use the built-in `WikiScreen`.

### Basic Usage
To open a wiki populated with markdown files from your mod's assets (`assets/<modid>/wiki/`):
```java
PhoenixWikiAPI.open(currentScreen, "your_mod_id");
```

### Advanced Usage
You can specify a custom base path and a theme:
```java
PhoenixWikiAPI.open(parent, "your_mod_id", "docs/wiki", WikiTheme.DEFAULT);
```
- **Namespace**: The mod ID to look for assets in.
- **Base Path**: The folder inside `assets/<namespace>/` containing your `.md` files (defaults to `wiki`).
- **Theme**: A `WikiTheme` record defining colors for the UI.

## 2. Shared Theming
PhoenixWiki provides a robust theming system via `PhoenixTheme` that can be shared across all PhoenixSuite-style mods.

### Accessing Colors
Use `PhoenixTheme.current()` to get the active theme colors:
```java
PhoenixTheme theme = PhoenixTheme.current();
int accent = theme.accent.getColor();
int background = theme.bg.getColor();
```

### Theming your own UI
If you want your custom GUI to respect the user's selected PhoenixTheme, use the `ThemeColor` properties which handle hex parsing and animations (like breathing/pulsing effects).

## 3. Markdown Parsing & Rendering
If you want to render markdown in your own custom screens without using the full `WikiScreen`, you can use the rich text engine directly.

### Parsing
Convert a raw string into a list of `RichBlock`s:
```java
List<RichBlock> blocks = WikiMarkdownParser.parse(markdownString);
```

### Rendering
Draw the parsed blocks into a `GuiGraphics` context:
```java
WikiRichTextRenderer.renderBlocks(
    guiGraphics, 
    font, 
    blocks, 
    x, y, maxWidth, 
    scrollY, 
    clipTop, clipBottom, 
    theme.accent.getColor()
);
```

### Measuring
Calculate the total height the blocks will take to handle scrolling:
```java
int totalHeight = WikiRichTextRenderer.measureBlocksHeight(font, blocks, maxWidth);
```

## 4. Extending the Parser
You can add custom markdown syntax or custom rendering for existing blocks:
- **`BlockParserRegistry.DEFAULT`**: Register new `BlockParser`s for custom block-level syntax.
- **`InlineHandlerRegistry.DEFAULT`**: Register new `InlineHandler`s for custom inline syntax
- (e.g., custom color codes).
- **`BlockRendererRegistry.DEFAULT`**: Register new `BlockRenderer`s to change how specific 
- `RichBlock` types are drawn.

# Roadmap
Wiki needs: 
- Better documentation.
- Ported to 1.21.1.
- Eventually be given more markdown features.

Documentation is the priority since so many mods depend on Wiki.

# Markdown examples.

Some of the included 

# Claiming to be an Omind project.

# Credits

# Ai disclosure.


# Where to go next.
Contributing
See CONTRIBUTING.md.

Architecture Decisions
See Architecture.md.

Frequently Asked Questions
See FAQ.md

Known Issues
See KNOWN-ISSUES.md