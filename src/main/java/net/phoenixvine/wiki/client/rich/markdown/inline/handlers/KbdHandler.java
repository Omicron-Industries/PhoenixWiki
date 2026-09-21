package net.phoenixvine.wiki.client.rich.markdown.inline.handlers;

import net.minecraft.network.chat.TextColor;
import net.phoenixvine.wiki.client.rich.RichSpan;
import net.phoenixvine.wiki.client.rich.markdown.inline.InlineHandler;
import net.phoenixvine.wiki.client.rich.markdown.inline.InlineParseState;

public final class KbdHandler implements InlineHandler {

    private static final int KBD_COLOR = 0xFFD0D0D8;
    private static final int KBD_BG = 0xFF2B2B33;

    @Override
    public char trigger() {
        return '<';
    }

    @Override
    public int tryHandle(InlineParseState s, int i) {
        if (!s.input.startsWith("<kbd>", i)) return -1;
        int end = s.input.indexOf("</kbd>", i + 5);
        if (end <= i) return -1;
        s.flush();
        s.out.add(new RichSpan.Text(" " + s.input.substring(i + 5, end) + " ",
                s.style.withColor(TextColor.fromRgb(KBD_COLOR)).withBold(true), KBD_BG, null, s.scale));
        return end + 6;
    }
}
