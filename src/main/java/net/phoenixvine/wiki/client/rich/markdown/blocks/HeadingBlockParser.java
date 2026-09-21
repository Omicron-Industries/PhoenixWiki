package net.phoenixvine.wiki.client.rich.markdown.blocks;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.markdown.BlockParser;
import net.phoenixvine.wiki.client.rich.markdown.MarkdownPatterns;
import net.phoenixvine.wiki.client.rich.markdown.ParseContext;

import java.util.List;
import java.util.regex.Matcher;

public final class HeadingBlockParser implements BlockParser {
    @Override
    public int tryParse(String[] lines, int i, List<RichBlock> out, ParseContext ctx) {
        Matcher m = MarkdownPatterns.HEADING.matcher(lines[i].trim());
        if (!m.matches()) return -1;
        int level = m.group(1).length();
        out.add(new RichBlock.Heading(level, ctx.parseInline(m.group(2))));
        return i + 1;
    }
}
