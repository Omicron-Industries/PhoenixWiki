package net.phoenixvine.wiki.client.rich.markdown.blocks;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.markdown.BlockParser;
import net.phoenixvine.wiki.client.rich.markdown.MarkdownPatterns;
import net.phoenixvine.wiki.client.rich.markdown.ParseContext;

import java.util.List;
import java.util.regex.Matcher;

public final class ScaleDirectiveBlockParser implements BlockParser {
    @Override
    public int tryParse(String[] lines, int i, List<RichBlock> out, ParseContext ctx) {
        Matcher m = MarkdownPatterns.SCALE_DIRECTIVE.matcher(lines[i].trim());
        if (!m.matches()) return -1;
        try {
            out.add(new RichBlock.ScaleDirective(Float.parseFloat(m.group(1))));
        } catch (NumberFormatException ignored) {}
        return i + 1;
    }
}
