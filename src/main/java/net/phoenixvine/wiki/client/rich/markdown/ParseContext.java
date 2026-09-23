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

    /**
     * When true, a plain continuation line right after a list item (no marker of its own) is
     * appended into that item instead of breaking into its own paragraph -- opt-in, default off,
     * so existing content that relies on "every line is its own thing" keeps working unchanged.
     */
    public boolean slurpListContinuations() {
        return slurpListContinuations;
    }

    /**
     * When true, a single newline inside a paragraph becomes a real line break instead of the
     * usual CommonMark "soft wrap" (collapsed to a space) -- opt-in, default off. Content
     * hand-typed in a wrapping editor (wiki pages, Guild descriptions) wants the standard
     * soft-wrap behavior; content sourced from a lang file, where the author put a literal
     * {@code \n} exactly where they wanted a break, doesn't.
     */
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
