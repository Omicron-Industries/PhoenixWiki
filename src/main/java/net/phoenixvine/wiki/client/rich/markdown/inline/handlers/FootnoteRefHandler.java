package net.phoenixvine.wiki.client.rich.markdown.inline.handlers;

import net.minecraft.network.chat.TextColor;
import net.phoenixvine.wiki.client.rich.RichSpan;
import net.phoenixvine.wiki.client.rich.markdown.inline.InlineHandler;
import net.phoenixvine.wiki.client.rich.markdown.inline.InlineParseState;


public final class FootnoteRefHandler implements InlineHandler {

    @Override
    public char trigger() {
        return '[';
    }

    @Override
    public int tryHandle(InlineParseState s, int i) {
        if (i + 1 >= s.length() || s.charAt(i + 1) != '^') return -1;
        int end = s.input.indexOf(']', i + 2);
        if (end <= i) return -1;

        String id = s.input.substring(i + 2, end);
        String def = s.footnotes.get(id);
        s.flush();
        if (def != null) {
            s.out.add(new RichSpan.Tip("[" + id + "]", s.style.withColor(TextColor.fromRgb(0xFFAAFFAA)), def));
        } else {
            s.out.add(new RichSpan.Text("[^" + id + "]", s.style));
        }
        return end + 1;
    }
}
