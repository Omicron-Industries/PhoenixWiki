package net.phoenixvine.wiki.client.rich.markdown;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.RichSpan;
import net.phoenixvine.wiki.client.rich.markdown.inline.InlineParser;

import java.util.List;
import java.util.Map;


public final class ParseContext {

    private final Map<String, String> footnotes;
    private final BlockParserRegistry registry;

    ParseContext(Map<String, String> footnotes, BlockParserRegistry registry) {
        this.footnotes = footnotes;
        this.registry = registry;
    }

    public Map<String, String> footnotes() {
        return footnotes;
    }

    public List<RichBlock> parseLines(String[] lines) {
        return registry.parseLines(lines, this);
    }

    public List<RichSpan> parseInline(String text) {
        return InlineParser.parse(text, footnotes);
    }
}
