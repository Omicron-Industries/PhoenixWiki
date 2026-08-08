package net.phoenixvine.wiki.client.screen;

public record WikiTheme(int bg, int panel, int header, int border, int accent, int text, int textDim,
                        int textFaint, int done, int active) {

    public static final WikiTheme DEFAULT = new WikiTheme(
            0xFF16121F, 
            0xFF1E1830, 
            0xFF241D38, 
            0xFF3A3040, 
            0xFF9966FF, 
            0xFFE0E0E0, 
            0xFFAAAAAA, 
            0xFF6A6478, 
            0xFF50C878, 
            0xFF55AAFF  
    );
}
