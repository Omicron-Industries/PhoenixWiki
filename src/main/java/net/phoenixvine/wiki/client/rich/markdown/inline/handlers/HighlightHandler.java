package net.phoenixvine.wiki.client.rich.markdown.inline.handlers;

import net.phoenixvine.wiki.client.rich.markdown.inline.InlineHandler;
import net.phoenixvine.wiki.client.rich.markdown.inline.InlineParseState;

public final class HighlightHandler implements InlineHandler {

    private static final int HIGHLIGHT_BG = 0x66E0C24A;

    @Override
    public char trigger() {
        return '=';
    }

    @Override
    public int tryHandle(InlineParseState s, int i) {
        if (i + 1 >= s.length() || s.charAt(i + 1) != '=') return -1;
        s.flush();
        s.background = s.background == HIGHLIGHT_BG ? 0 : HIGHLIGHT_BG;
        return i + 2;
    }
}
