package net.phoenixvine.wiki.client.rich.markdown.blocks;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.markdown.BlockParser;
import net.phoenixvine.wiki.client.rich.markdown.MarkdownPatterns;
import net.phoenixvine.wiki.client.rich.markdown.ParseContext;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;


public final class ContainerBlockParser implements BlockParser {
    @Override
    public int tryParse(String[] lines, int i, List<RichBlock> out, ParseContext ctx) {
        Matcher open = MarkdownPatterns.CONTAINER_OPEN.matcher(lines[i].trim());
        if (!open.matches()) return -1;

        String type = open.group(1).toLowerCase();
        String title = open.group(2).trim();
        int depth = 1;
        int start = i + 1;
        int j = start;
        while (j < lines.length && depth > 0) {
            String t = lines[j].trim();
            if (MarkdownPatterns.CONTAINER_CLOSE.matcher(t).matches()) {
                depth--;
                if (depth == 0) break;
            } else if (MarkdownPatterns.CONTAINER_OPEN.matcher(t).matches()) {
                depth++;
            }
            j++;
        }
        String[] inner = Arrays.copyOfRange(lines, start, Math.min(j, lines.length));
        List<RichBlock> children = ctx.parseLines(inner);
        if (type.equals("spoiler") || type.equals("details")) {
            String key = (title.isEmpty() ? "section" : title) + "#" + start;
            out.add(new RichBlock.Details(key, title.isEmpty() ? "Details" : title, children));
        } else {
            out.add(new RichBlock.Callout(type, title, children));
        }
        return j + 1;
    }
}
