package net.phoenixvine.wiki.client.rich.markdown.blocks;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.markdown.BlockParser;
import net.phoenixvine.wiki.client.rich.markdown.MarkdownPatterns;
import net.phoenixvine.wiki.client.rich.markdown.ParseContext;

import java.util.List;
import java.util.regex.Matcher;

public final class OrderedListBlockParser implements BlockParser {
    @Override
    public int tryParse(String[] lines, int i, List<RichBlock> out, ParseContext ctx) {
        Matcher om = MarkdownPatterns.ORDERED.matcher(lines[i].trim());
        if (!om.matches()) return -1;
        String marker = om.group(1) + ".";
        StringBuilder item = new StringBuilder(om.group(2));
        int next = i + 1;
        if (ctx.slurpListContinuations()) next = MarkdownPatterns.slurpListContinuation(lines, next, item);
        out.add(new RichBlock.ListItem(marker, Math.max(14, estimateMarkerWidth(marker)),
                ctx.parseInline(item.toString())));
        return next;
    }

    private static int estimateMarkerWidth(String marker) {
        return 6 * marker.length() + 6;
    }
}
