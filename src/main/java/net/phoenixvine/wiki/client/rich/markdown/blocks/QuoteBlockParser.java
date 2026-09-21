package net.phoenixvine.wiki.client.rich.markdown.blocks;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.markdown.BlockParser;
import net.phoenixvine.wiki.client.rich.markdown.MarkdownPatterns;
import net.phoenixvine.wiki.client.rich.markdown.ParseContext;

import java.util.List;
import java.util.regex.Matcher;

public final class QuoteBlockParser implements BlockParser {
    @Override
    public int tryParse(String[] lines, int i, List<RichBlock> out, ParseContext ctx) {
        Matcher qm = MarkdownPatterns.QUOTE.matcher(lines[i].trim());
        if (!qm.matches()) return -1;
        StringBuilder quote = new StringBuilder();
        int j = i;
        while (j < lines.length) {
            Matcher q = MarkdownPatterns.QUOTE.matcher(lines[j].trim());
            if (!q.matches()) break;
            if (!quote.isEmpty()) quote.append(' ');
            quote.append(q.group(1));
            j++;
        }
        out.add(new RichBlock.Quote(ctx.parseInline(quote.toString())));
        return j;
    }
}
