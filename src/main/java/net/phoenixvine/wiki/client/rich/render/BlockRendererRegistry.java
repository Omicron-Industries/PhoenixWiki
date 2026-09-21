package net.phoenixvine.wiki.client.rich.render;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.render.blocks.CalloutBlockRenderer;
import net.phoenixvine.wiki.client.rich.render.blocks.ChecklistBlockRenderer;
import net.phoenixvine.wiki.client.rich.render.blocks.CodeBlockRenderer;
import net.phoenixvine.wiki.client.rich.render.blocks.CollapsibleSectionBlockRenderer;
import net.phoenixvine.wiki.client.rich.render.blocks.DetailsBlockRenderer;
import net.phoenixvine.wiki.client.rich.render.blocks.HeadingBlockRenderer;
import net.phoenixvine.wiki.client.rich.render.blocks.ListItemBlockRenderer;
import net.phoenixvine.wiki.client.rich.render.blocks.ParagraphBlockRenderer;
import net.phoenixvine.wiki.client.rich.render.blocks.QuoteBlockRenderer;
import net.phoenixvine.wiki.client.rich.render.blocks.RuleBlockRenderer;
import net.phoenixvine.wiki.client.rich.render.blocks.ScaleDirectiveBlockRenderer;
import net.phoenixvine.wiki.client.rich.render.blocks.TableBlockRenderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class BlockRendererRegistry {

    public static final BlockRendererRegistry DEFAULT = new BlockRendererRegistry()
            .register(RichBlock.ScaleDirective.class, new ScaleDirectiveBlockRenderer())
            .register(RichBlock.Heading.class, new HeadingBlockRenderer())
            .register(RichBlock.CollapsibleSection.class, new CollapsibleSectionBlockRenderer())
            .register(RichBlock.ListItem.class, new ListItemBlockRenderer())
            .register(RichBlock.Checklist.class, new ChecklistBlockRenderer())
            .register(RichBlock.Paragraph.class, new ParagraphBlockRenderer())
            .register(RichBlock.Rule.class, new RuleBlockRenderer())
            .register(RichBlock.CodeBlock.class, new CodeBlockRenderer())
            .register(RichBlock.Quote.class, new QuoteBlockRenderer())
            .register(RichBlock.Table.class, new TableBlockRenderer())
            .register(RichBlock.Callout.class, new CalloutBlockRenderer())
            .register(RichBlock.Details.class, new DetailsBlockRenderer());

    private final Map<Class<? extends RichBlock>, BlockRenderer<? extends RichBlock>> renderers = new HashMap<>();

    public <T extends RichBlock> BlockRendererRegistry register(Class<T> type, BlockRenderer<T> renderer) {
        renderers.put(type, renderer);
        return this;
    }

    @SuppressWarnings("unchecked")
    private BlockRenderer<RichBlock> rendererFor(RichBlock block) {
        return (BlockRenderer<RichBlock>) (BlockRenderer<?>) renderers.get(block.getClass());
    }

    public int renderAll(RenderContext ctx, List<RichBlock> blocks, int x, int y, int maxW) {
        int curY = y;
        boolean first = true;
        for (RichBlock block : blocks) {
            if (block instanceof RichBlock.Blank) {
                curY += SpanRenderer.GAP_BLANK;
                first = false;
                continue;
            }
            BlockRenderer<RichBlock> r = rendererFor(block);
            if (r == null) {
                first = false;
                continue;
            }
            if (!first) curY += r.gapBefore(block);
            first = false;
            curY = r.render(ctx, block, x, curY, maxW);
        }
        return curY;
    }

    public int measureAll(RenderContext ctx, List<RichBlock> blocks, int y, int maxW) {
        int curY = y;
        boolean first = true;
        for (RichBlock block : blocks) {
            if (block instanceof RichBlock.Blank) {
                curY += SpanRenderer.GAP_BLANK;
                first = false;
                continue;
            }
            BlockRenderer<RichBlock> r = rendererFor(block);
            if (r == null) {
                first = false;
                continue;
            }
            if (!first) curY += r.gapBefore(block);
            first = false;
            curY = r.measure(ctx, block, curY, maxW);
        }
        return curY;
    }

    public int gapBeforeFor(RichBlock block) {
        BlockRenderer<RichBlock> r = rendererFor(block);
        return r == null ? SpanRenderer.GAP_PARAGRAPH : r.gapBefore(block);
    }

    public int measureOne(RenderContext ctx, RichBlock block, int y, int maxW) {
        BlockRenderer<RichBlock> r = rendererFor(block);
        return r == null ? y : r.measure(ctx, block, y, maxW);
    }
}
