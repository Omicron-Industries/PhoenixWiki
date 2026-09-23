package net.phoenixvine.wiki.client.rich.markdown;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.RichSpan;
import net.phoenixvine.wiki.client.rich.markdown.blocks.ContainerBlockParser;
import net.phoenixvine.wiki.client.rich.markdown.blocks.FenceBlockParser;
import net.phoenixvine.wiki.client.rich.markdown.blocks.HeadingBlockParser;
import net.phoenixvine.wiki.client.rich.markdown.blocks.OrderedListBlockParser;
import net.phoenixvine.wiki.client.rich.markdown.blocks.ParagraphBlockParser;
import net.phoenixvine.wiki.client.rich.markdown.blocks.QuoteBlockParser;
import net.phoenixvine.wiki.client.rich.markdown.blocks.RuleBlockParser;
import net.phoenixvine.wiki.client.rich.markdown.blocks.ScaleDirectiveBlockParser;
import net.phoenixvine.wiki.client.rich.markdown.blocks.TableBlockParser;
import net.phoenixvine.wiki.client.rich.markdown.blocks.UnorderedListBlockParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public final class BlockParserRegistry {

    public static final BlockParserRegistry DEFAULT = new BlockParserRegistry()
            .register(new ScaleDirectiveBlockParser())
            .register(new HeadingBlockParser())
            .register(new ContainerBlockParser())
            .register(new UnorderedListBlockParser())
            .register(new OrderedListBlockParser())
            .register(new RuleBlockParser())
            .register(new FenceBlockParser())
            .register(new TableBlockParser())
            .register(new QuoteBlockParser())
            .register(new ParagraphBlockParser());

    private final List<BlockParser> parsers = new ArrayList<>();

    public BlockParserRegistry register(BlockParser parser) {
        parsers.add(parser);
        return this;
    }

    public BlockParserRegistry registerBeforeFallback(BlockParser parser) {
        int idx = parsers.isEmpty() ? 0 : parsers.size() - 1;
        parsers.add(idx, parser);
        return this;
    }

    /**
     * Gives {@code parser} first refusal on every line, ahead of every built-in (including
     * {@link net.phoenixvine.wiki.client.rich.markdown.blocks.ContainerBlockParser}) -- for a
     * mod that needs to recognize its own {@code :::type} container before the generic one
     * claims it as a plain {@link net.phoenixvine.wiki.client.rich.RichBlock.Callout}.
     */
    public BlockParserRegistry registerFirst(BlockParser parser) {
        parsers.add(0, parser);
        return this;
    }

    List<RichBlock> parseLines(String[] lines, ParseContext ctx) {
        List<RichBlock> blocks = new ArrayList<>();
        int i = 0;
        boolean lastWasBlank = true;
        while (i < lines.length) {
            String trimmed = lines[i].trim();
            if (trimmed.isEmpty()) {
                if (!lastWasBlank) blocks.add(new RichBlock.Blank());
                lastWasBlank = true;
                i++;
                continue;
            }
            lastWasBlank = false;

            int next = -1;
            for (BlockParser parser : parsers) {
                next = parser.tryParse(lines, i, blocks, ctx);
                if (next >= 0) break;
            }
            i = next >= 0 ? next : i + 1;
        }
        return blocks;
    }

    public List<RichBlock> parse(String[] lines, Map<String, List<RichSpan.TipCandidate>> footnotes) {
        return parse(lines, footnotes, false, false);
    }

    public List<RichBlock> parse(String[] lines, Map<String, List<RichSpan.TipCandidate>> footnotes,
                                 boolean slurpListContinuations) {
        return parse(lines, footnotes, slurpListContinuations, false);
    }

    public List<RichBlock> parse(String[] lines, Map<String, List<RichSpan.TipCandidate>> footnotes,
                                 boolean slurpListContinuations, boolean hardLineBreaks) {
        return parseLines(lines, new ParseContext(footnotes, this, slurpListContinuations, hardLineBreaks));
    }
}
