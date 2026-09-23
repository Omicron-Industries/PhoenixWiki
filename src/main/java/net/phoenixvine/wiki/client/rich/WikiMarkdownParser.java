package net.phoenixvine.wiki.client.rich;

import net.phoenixvine.wiki.client.rich.markdown.BlockParserRegistry;
import net.phoenixvine.wiki.client.rich.markdown.FootnoteExtractor;
import net.phoenixvine.wiki.client.rich.markdown.HeadingSectionGrouper;

import java.util.List;

public final class WikiMarkdownParser {

    private WikiMarkdownParser() {}

    public static List<RichBlock> parse(String input) {
        return parse(input, false);
    }

    public static List<RichBlock> parse(String input, boolean slurpListContinuations) {
        return parse(input, slurpListContinuations, false);
    }

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
