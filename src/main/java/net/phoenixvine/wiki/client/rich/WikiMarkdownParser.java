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
        if (input == null || input.isBlank()) return List.of();

        String[] rawLines = input.replace("\r\n", "\n")
                .replace("\r", "\n").split("\n", -1);

        FootnoteExtractor.Result footnoted = FootnoteExtractor.extract(rawLines);
        List<RichBlock> blocks = BlockParserRegistry.DEFAULT.parse(footnoted.lines(), footnoted.footnotes());
        return HeadingSectionGrouper.group(blocks);
    }
}
