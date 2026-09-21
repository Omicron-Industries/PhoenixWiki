package net.phoenixvine.wiki.client.rich.markdown.inline.handlers;

import net.phoenixvine.wiki.client.rich.markdown.inline.InlineHandler;
import net.phoenixvine.wiki.client.rich.markdown.inline.InlineParseState;

public final class StrikethroughHandler implements InlineHandler {

    @Override
    public char trigger() {
        return '~';
    }

    @Override
    public int tryHandle(InlineParseState s, int i) {
        if (i + 1 >= s.length() || s.charAt(i + 1) != '~') return -1;
        s.flush();
        s.style = s.style.withStrikethrough(!s.style.isStrikethrough());
        return i + 2;
    }
}
