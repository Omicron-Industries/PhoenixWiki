package net.phoenixvine.wiki.client.rich.markdown.inline.handlers;

import net.minecraft.network.chat.TextColor;
import net.phoenixvine.wiki.client.rich.RichSpan;
import net.phoenixvine.wiki.client.rich.markdown.inline.InlineHandler;
import net.phoenixvine.wiki.client.rich.markdown.inline.InlineParseState;

public final class InlineCodeHandler implements InlineHandler {

    private static final int CODE_COLOR = 0xFFD37A;
    private static final int CODE_BG = 0x501A1622;

    @Override
    public char trigger() {
        return '`';
    }

    @Override
    public int tryHandle(InlineParseState s, int i) {
        int end = s.input.indexOf('`', i + 1);
        if (end <= i) return -1;
        s.flush();
        String codeText = s.input.substring(i + 1, end);
        s.out.add(new RichSpan.Text(codeText, s.style.withColor(TextColor.fromRgb(CODE_COLOR)), CODE_BG, codeText,
                s.scale));
        return end + 1;
    }
}
