package net.phoenixvine.wiki.client.rich.markdown.inline.handlers;

import net.phoenixvine.wiki.client.rich.markdown.inline.InlineHandler;
import net.phoenixvine.wiki.client.rich.markdown.inline.InlineParseState;

public final class BoldItalicHandler implements InlineHandler {

    @Override
    public char trigger() {
        return '*';
    }

    @Override
    public int tryHandle(InlineParseState s, int i) {
        s.flush();
        if (i + 1 < s.length() && s.charAt(i + 1) == '*') {
            s.style = s.style.withBold(!s.style.isBold());
            return i + 2;
        }
        s.style = s.style.withItalic(!s.style.isItalic());
        return i + 1;
    }
}
