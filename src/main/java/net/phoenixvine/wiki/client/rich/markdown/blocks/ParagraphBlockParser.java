package net.phoenixvine.wiki.client.rich.markdown.blocks;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.markdown.BlockParser;
import net.phoenixvine.wiki.client.rich.markdown.MarkdownPatterns;
import net.phoenixvine.wiki.client.rich.markdown.ParseContext;

import java.util.List;

/**
 * Unconditional fallback: collects lines into a paragraph until it hits a blank line or the
 * start of any other known block type. Must stay registered last - see
 * {@link net.phoenixvine.wiki.client.rich.markdown.BlockParserRegistry}.
 */
public final class ParagraphBlockParser implements BlockParser {
    @Override
    public int tryParse(String[] lines, int i, List<RichBlock> out, ParseContext ctx) {
        StringBuilder para = new StringBuilder();
        int j = i;
        while (j < lines.length) {
            String t = lines[j].trim();
            if (t.isEmpty() || isOtherBlockStart(lines, j, t)) break;
            if (!para.isEmpty()) para.append(' ');
            para.append(t);
            j++;
        }
        out.add(new RichBlock.Paragraph(ctx.parseInline(para.toString())));
        return j;
    }

    private static boolean isOtherBlockStart(String[] lines, int j, String t) {
        return MarkdownPatterns.HEADING.matcher(t).matches()
                || MarkdownPatterns.UNORDERED.matcher(t).matches()
                || MarkdownPatterns.ORDERED.matcher(t).matches()
                || MarkdownPatterns.RULE.matcher(t).matches()
                || MarkdownPatterns.FENCE.matcher(t).matches()
                || MarkdownPatterns.QUOTE.matcher(t).matches()
                || MarkdownPatterns.CONTAINER_OPEN.matcher(t).matches()
                || MarkdownPatterns.CONTAINER_CLOSE.matcher(t).matches()
                || MarkdownPatterns.isTableStart(lines, j);
    }
}
