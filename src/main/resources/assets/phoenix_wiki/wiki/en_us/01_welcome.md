# Welcome to Phoenix Wiki

This is a sample page shipped with the **Phoenix Wiki** library mod itself, so
`phoenix_wiki`'s own wiki has something to show if you open it directly for testing.

Host mods should ship their own pages under
`assets/<your_mod_id>/wiki/<locale>/*.md` and call:

```java
PhoenixWikiAPI.open(parentScreen, "your_mod_id");
```

## Formatting cheat sheet

- **bold**, *italic*, ~~strikethrough~~, ==highlight==, `inline code`
- Press <kbd>Ctrl</kbd>+<kbd>C</kbd> on a code block or inline code span to copy it
- [ ] An unchecked checklist item
- [x] A checked checklist item

> A blockquote, for callouts or asides.

```java
public static void main(String[] args) {
    System.out.println("Hello from Phoenix Wiki!");
}
```

## Chapters

Put a page in a one-level subfolder (e.g. `wiki/en_us/getting_started/01_intro.md`)
to group it under a collapsible "Getting Started" chapter in the sidebar.
