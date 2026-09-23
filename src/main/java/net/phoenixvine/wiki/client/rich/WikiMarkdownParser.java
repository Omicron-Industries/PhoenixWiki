package net.phoenixvine.wiki.client.rich;

import net.phoenixvine.wiki.client.rich.markdown.BlockParserRegistry;
import net.phoenixvine.wiki.client.rich.markdown.FootnoteExtractor;
import net.phoenixvine.wiki.client.rich.markdown.HeadingSectionGrouper;

import java.util.List;

/**
 * Entry point for turning wiki markdown source into a {@link RichBlock} tree.
 *
 * The grammar itself lives in {@code net.phoenixvine.wiki.client.rich.markdown} as a set of
 * small, independently registered
 * {@link net.phoenixvine.wiki.client.rich.markdown.BlockParser}s and
 * {@link net.phoenixvine.wiki.client.rich.markdown.inline.InlineHandler}s. To add a new block or
 * inline construct, write one of those and register it on
 * {@link BlockParserRegistry#DEFAULT} /
 * {@link net.phoenixvine.wiki.client.rich.markdown.inline.InlineHandlerRegistry#DEFAULT} rather
 * than editing this class.
 */
public final class WikiMarkdownParser {

    private WikiMarkdownParser() {}

    public static List<RichBlock> parse(String input) {
        return parse(input, false);
    }

    /**
     * @param slurpListContinuations opt-in: when true, a plain continuation line right after a
     *                               list item gets appended into that item instead of breaking
     *                               into its own paragraph. Default (the no-arg overload) is
     *                               off, matching every existing caller's behavior.
     */
    public static List<RichBlock> parse(String input, boolean slurpListContinuations) {
        return parse(input, slurpListContinuations, false);
    }

    /**
     * @param hardLineBreaks opt-in: when true, a single newline inside a paragraph becomes a
     *                       real line break instead of the usual CommonMark "soft wrap"
     *                       (collapsed to a space). Default off. Meant for content sourced from
     *                       a lang file, where a literal {@code \n} an author typed is an
     *                       intentional break, not incidental line-wrapping.
     */
    public static List<RichBlock> parse(String input, boolean slurpListContinuations, boolean hardLineBreaks) {
        if (input == null || input.isBlank()) return List.of();

        String[] rawLines = input.replace("\r\n", "\n")
                .replace("\r", "\n").split("\n", -1);

        FootnoteExtractor.Result footnoted = FootnoteExtractor.extract(rawLines);
        List<RichBlock> blocks = BlockParserRegistry.DEFAULT.parse(footnoted.lines(), footnoted.footnotes(),
                slurpListContinuations, hardLineBreaks);
        return HeadingSectionGrouper.group(blocks);
    }
}
