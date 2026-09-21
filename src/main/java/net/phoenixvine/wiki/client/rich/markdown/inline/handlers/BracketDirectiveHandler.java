package net.phoenixvine.wiki.client.rich.markdown.inline.handlers;

import net.minecraft.resources.ResourceLocation;
import net.phoenixvine.wiki.client.rich.RichSpan;
import net.phoenixvine.wiki.client.rich.markdown.inline.ImageSpans;
import net.phoenixvine.wiki.client.rich.markdown.inline.InlineHandler;
import net.phoenixvine.wiki.client.rich.markdown.inline.InlineParseState;

public final class BracketDirectiveHandler implements InlineHandler {

    @Override
    public char trigger() {
        return '[';
    }

    @Override
    public int tryHandle(InlineParseState s, int i) {
        int bracketEnd = s.input.indexOf(']', i + 1);
        if (bracketEnd <= i) return -1;
        String inner = s.input.substring(i + 1, bracketEnd);

        if (inner.startsWith("img:")) {
            s.flush();
            ImageSpans.addImage(s.out, inner.substring(4));
            return bracketEnd + 1;
        }
        if (inner.startsWith("item:")) {
            s.flush();
            String itemPart = inner.substring(5);
            int bar = itemPart.indexOf('|');
            String idPart = bar >= 0 ? itemPart.substring(0, bar) : itemPart;
            String tooltip = bar >= 0 ? itemPart.substring(bar + 1).trim() : null;
            if (tooltip != null && tooltip.isEmpty()) tooltip = null;
            try {
                s.out.add(new RichSpan.ItemIcon(ResourceLocation.parse(idPart.trim()), tooltip));
            } catch (Exception ignored) {}
            return bracketEnd + 1;
        }
        return -1;
    }
}
