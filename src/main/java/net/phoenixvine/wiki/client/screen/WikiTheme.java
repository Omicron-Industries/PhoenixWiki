package net.phoenixvine.wiki.client.screen;

/** The 10 colors WikiScreen needs. Pass your own via the WikiScreen constructor, or use DEFAULT. */
public record WikiTheme(int bg, int panel, int header, int border, int accent, int text, int textDim,
                        int textFaint, int done, int active) {

    public static final WikiTheme DEFAULT = new WikiTheme(
            0xFF16121F, // bg
            0xFF1E1830, // panel
            0xFF241D38, // header
            0xFF3A3040, // border
            0xFF9966FF, // accent
            0xFFE0E0E0, // text
            0xFFAAAAAA, // textDim
            0xFF6A6478, // textFaint
            0xFF50C878, // done (unused by WikiScreen itself, kept for host-mod reuse)
            0xFF55AAFF  // active
    );
}
