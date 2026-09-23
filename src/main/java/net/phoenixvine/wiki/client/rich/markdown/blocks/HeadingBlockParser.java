package net.phoenixvine.wiki.client.rich.markdown.blocks;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.markdown.BlockParser;
import net.phoenixvine.wiki.client.rich.markdown.MarkdownPatterns;
import net.phoenixvine.wiki.client.rich.markdown.ParseContext;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HeadingBlockParser implements BlockParser {

    private static final Pattern FLAT_MARKER = Pattern.compile("(?i)\\s*\\{(?:flat|nocollapse)}\\s*$");
    private static final Pattern COLLAPSE_MARKER = Pattern.compile("(?i)\\s*\\{collapse}\\s*$");

    @Override
    public int tryParse(String[] lines, int i, List<RichBlock> out, ParseContext ctx) {
        Matcher m = MarkdownPatterns.HEADING.matcher(lines[i].trim());
        if (!m.matches()) return -1;
        int level = m.group(1).length();
        String headingText = m.group(2);

        boolean collapsible = true;
        Matcher flat = FLAT_MARKER.matcher(headingText);
        if (flat.find()) {
            collapsible = false;
            headingText = flat.replaceFirst("");
        } else {
            // {collapse} is a no-op now that collapsible is the default -- still stripped so it
            // never leaks into rendered heading text for existing content that tags it.
            Matcher collapse = COLLAPSE_MARKER.matcher(headingText);
            if (collapse.find()) headingText = collapse.replaceFirst("");
        }

        out.add(new RichBlock.Heading(level, ctx.parseInline(headingText), collapsible));
        return i + 1;
    }
}
