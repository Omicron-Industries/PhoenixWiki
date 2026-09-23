package net.phoenixvine.wiki.client.rich.markdown.blocks;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.markdown.BlockParser;
import net.phoenixvine.wiki.client.rich.markdown.MarkdownPatterns;
import net.phoenixvine.wiki.client.rich.markdown.ParseContext;

import java.util.List;
import java.util.regex.Matcher;

public final class UnorderedListBlockParser implements BlockParser {
    @Override
    public int tryParse(String[] lines, int i, List<RichBlock> out, ParseContext ctx) {
        Matcher um = MarkdownPatterns.UNORDERED.matcher(lines[i]);
        if (!um.matches()) return -1;
        int nestLevel = um.group(1).length() / 2;
        Matcher cb = MarkdownPatterns.CHECKBOX.matcher(um.group(2));
        if (cb.matches()) {
            boolean checked = !cb.group(1).equals(" ");
            String key = "cl#" + i;
            out.add(new RichBlock.Checklist(key, checked, 10 + nestLevel * 10, ctx.parseInline(cb.group(2))));
            return i + 1;
        }

        StringBuilder item = new StringBuilder(um.group(2));
        int next = i + 1;
        if (ctx.slurpListContinuations()) next = MarkdownPatterns.slurpListContinuation(lines, next, item);
        out.add(new RichBlock.ListItem("\u2022", 10 + nestLevel * 10, ctx.parseInline(item.toString())));
        return next;
    }
}
