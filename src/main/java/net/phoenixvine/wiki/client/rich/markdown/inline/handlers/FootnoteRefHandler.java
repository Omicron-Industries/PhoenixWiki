package net.phoenixvine.wiki.client.rich.markdown.inline.handlers;

import net.minecraft.network.chat.TextColor;
import net.phoenixvine.wiki.client.rich.RichSpan;
import net.phoenixvine.wiki.client.rich.markdown.inline.InlineHandler;
import net.phoenixvine.wiki.client.rich.markdown.inline.InlineParseState;

import java.util.List;


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
        List<RichSpan.TipCandidate> candidates = s.footnotes.get(id);
        s.flush();
        var tipStyle = s.style.withColor(TextColor.fromRgb(0xFFAAFFAA));
        if (candidates == null) {
            s.out.add(new RichSpan.Text("[^" + id + "]", s.style));
        } else if (candidates.size() == 1 && isBlank(candidates.get(0).conditionExpr())) {
            s.out.add(new RichSpan.Tip("[" + id + "]", tipStyle, candidates.get(0).tooltip()));
        } else {
            s.out.add(new RichSpan.ConditionalTip("[" + id + "]", tipStyle, candidates));
        }
        return end + 1;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
