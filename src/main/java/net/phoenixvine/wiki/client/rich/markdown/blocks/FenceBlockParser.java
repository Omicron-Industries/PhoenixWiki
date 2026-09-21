package net.phoenixvine.wiki.client.rich.markdown.blocks;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.markdown.BlockParser;
import net.phoenixvine.wiki.client.rich.markdown.MarkdownPatterns;
import net.phoenixvine.wiki.client.rich.markdown.ParseContext;

import java.util.List;
import java.util.regex.Matcher;


public final class FenceBlockParser implements BlockParser {
    @Override
    public int tryParse(String[] lines, int i, List<RichBlock> out, ParseContext ctx) {
        Matcher fm = MarkdownPatterns.FENCE.matcher(lines[i].trim());
        if (!fm.matches()) return -1;
        String lang = fm.group(1);
        StringBuilder code = new StringBuilder();
        int j = i + 1;
        while (j < lines.length && !MarkdownPatterns.FENCE.matcher(lines[j].trim()).matches()) {
            if (!code.isEmpty()) code.append('\n');
            code.append(lines[j]);
            j++;
        }
        if (j < lines.length) j++;
        out.add(new RichBlock.CodeBlock(lang, code.toString()));
        return j;
    }
}
