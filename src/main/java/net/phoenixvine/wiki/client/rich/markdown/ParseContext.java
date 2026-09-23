package net.phoenixvine.wiki.client.rich.markdown;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.RichSpan;
import net.phoenixvine.wiki.client.rich.markdown.inline.InlineParser;

import java.util.List;
import java.util.Map;

public final class ParseContext {

    private final Map<String, List<RichSpan.TipCandidate>> footnotes;
    private final BlockParserRegistry registry;
    private final boolean slurpListContinuations;
    private final boolean hardLineBreaks;

    ParseContext(Map<String, List<RichSpan.TipCandidate>> footnotes, BlockParserRegistry registry,
                boolean slurpListContinuations, boolean hardLineBreaks) {
        this.footnotes = footnotes;
        this.registry = registry;
        this.slurpListContinuations = slurpListContinuations;
        this.hardLineBreaks = hardLineBreaks;
    }

    public Map<String, List<RichSpan.TipCandidate>> footnotes() {
        return footnotes;
    }

    public boolean slurpListContinuations() {
        return slurpListContinuations;
    }

    public boolean hardLineBreaks() {
        return hardLineBreaks;
    }

    public List<RichBlock> parseLines(String[] lines) {
        return registry.parseLines(lines, this);
    }

    public List<RichSpan> parseInline(String text) {
        return InlineParser.parse(text, footnotes);
    }
}
