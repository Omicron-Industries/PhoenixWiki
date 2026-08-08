# Phoenix Wiki

A small, self-contained in-game wiki/markdown engine for Forge 1.20.1, meant to be
jar-in-jar'd into other mods. It renders your mod's own markdown pages as a searchable,
in-game wiki screen with tables, callouts, collapsible sections, an on-page table of
contents, syntax-highlighted code blocks with copy buttons, clickable persistent
checklists, internal page links, live item icons, and chapter-grouped navigation.

Phoenix Wiki adds no content of its own - it's a library. Multiple host mods can embed
it at once; Forge's jar-in-jar deduplication loads a single shared copy, and every part
of the API takes your mod's own namespace/basePath/theme, so nothing collides between
host mods.

## Using it in your mod

### 1. Embed it

Build this project and drop the resulting jar somewhere your build can see it (a local
`libs/` folder + `flatDir` repo works fine for local development; a real Maven repo works
too once you publish it somewhere). Then, in your mod's `build.gradle`:

```groovy
repositories {
    flatDir { dir 'libs' }
}

dependencies {
    jarJar(modImplementation("local:phoenix_wiki:0.1.0"))
}
```

(`local` is just a placeholder group - flatDir repos don't check it, only the artifact
name and version need to match the jar's filename, e.g. `phoenix_wiki-0.1.0.jar`.)

### 2. Write your pages

Put markdown files at:

```
src/main/resources/assets/<your_mod_id>/wiki/en_us/01_getting_started.md
src/main/resources/assets/<your_mod_id>/wiki/en_us/02_advanced/01_something.md
```

- The leading `NN_` on a filename controls sidebar order and is stripped to form the
  page's id.
- The page title comes from the file's first `# Heading` line.
- A file placed one folder deeper (like `02_advanced/01_something.md` above) is grouped
  under a collapsible "Advanced" chapter in the sidebar; files directly under the locale
  folder are ungrouped.
- Locale falls back to `en_us` if the player's current locale has no matching pages.

Players can also edit pages in-game (the "+ New" / "✎ Edit" buttons) - those edits are
written to `config/<your_mod_id>/wiki/en_us/...` and overlay the shipped defaults, the
same shipped-vs-editable-copy split most data-driven Minecraft content uses.

### 3. Open it

```java
import net.phoenixvine.wiki.PhoenixWikiAPI;

// Default theme:
PhoenixWikiAPI.open(parentScreen, "your_mod_id");

// Custom basePath (defaults to "wiki"):
PhoenixWikiAPI.open(parentScreen, "your_mod_id", "docs");

// Custom theme, to match your own mod's UI:
PhoenixWikiAPI.open(parentScreen, "your_mod_id", "wiki", myTheme);
```

`WikiTheme` is a 10-color record (`bg`, `panel`, `header`, `border`, `accent`, `text`,
`textDim`, `textFaint`, `done`, `active`) - build one from your own theme system, or just
use `WikiTheme.DEFAULT`.

That's the entire integration surface. Everything else - the sidebar, search (which
scans actual rendered content, not just page titles), scrolling, table/callout/details
rendering, the TOC, internal links, code-block copy-to-clipboard, item icons, checklist
persistence - works out of the box, client-side only.

## Markdown syntax reference

Standard: `#`/`##` headings, `**bold**`, `*italic*`, `` `inline code` ``, fenced
` ``` ` code blocks (tag a language like ` ```java ` for syntax highlighting), `-`/`1.`
lists (2-space nesting), `---` horizontal rules, `| a | b |` pipe tables (wrap
automatically), `> quote` blockquotes.

Extensions:

| Syntax | Result |
|---|---|
| `{#RRGGBB}` / `{reset}` | inline color |
| `~~text~~` | strikethrough |
| `==text==` | highlighted text (real background fill, like a highlighter) |
| `` `inline code` `` | dark background pill; click to copy to clipboard |
| `<kbd>Key</kbd>` | keycap badge, e.g. `<kbd>Ctrl</kbd>` |
| `[img:resourcelocation,w,h]` | inline texture |
| `[item:resourcelocation]` | live-rendered item icon (real item model, no tooltip) |
| `[item:resourcelocation\|tooltip text]` | same, with a custom hover tooltip |
| `[label](url)` | external link (opens system browser) |
| `[label](wiki:page_id)` | internal link, jumps to another page in this wiki |
| `[label](tip:text)` | hover-only tooltip, no click |
| `[^id]` + `[^id]: definition` (anywhere in the file) | footnote, shown as a hover tooltip |
| `:::note` / `:::warning` / `:::tip` / `:::info` ... `:::` | colored callout box |
| `:::spoiler Title` ... `:::` | collapsible section |
| `- [ ] text` / `- [x] text` | clickable checklist item; checked state persists per player |

## Dynamic pages

If a specific page needs to substitute live data into its markdown (player stats, mod
counts, whatever), subclass `WikiScreen` and override `dynamicPageResolvers()`:

```java
@Override
protected Map<String, UnaryOperator<String>> dynamicPageResolvers() {
    return Map.of("live_stats", markdown -> markdown.replace("{{count}}", String.valueOf(getCount())));
}
```

Empty by default - most wikis don't need this.

## Custom textures

`[img:...]` resolves resource locations as-is by default. If you have your own runtime
texture-registration system, point the renderer at it once during client setup:

```java
WikiRichTextRenderer.imageResolver = YourTextureCache::resolve;
```

## Project layout

```
net.phoenixvine.wiki
├── PhoenixWiki.java          - @Mod entry point
├── PhoenixWikiAPI.java       - the public API described above
└── client
    ├── rich/                 - markdown parser + renderer (RichBlock/RichSpan model)
    └── screen/                - WikiScreen, WikiPageLoader, WikiTextInputScreen, etc.
```

No dependencies beyond vanilla Minecraft, Forge, and (at compile time only)
`org.jetbrains:annotations`.
