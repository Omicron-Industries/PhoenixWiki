package net.phoenixvine.wiki.client.rich.render.blocks;

import net.minecraft.network.chat.Style;
import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.RichSpan;
import net.phoenixvine.wiki.client.rich.render.BlockRenderer;
import net.phoenixvine.wiki.client.rich.render.RenderContext;
import net.phoenixvine.wiki.client.rich.render.SpanRenderer;

import java.util.ArrayList;
import java.util.List;

public final class CollapsibleSectionBlockRenderer implements BlockRenderer<RichBlock.CollapsibleSection> {

    @Override
    public int measure(RenderContext ctx, RichBlock.CollapsibleSection block, int y, int maxW) {
        List<RichSpan> styled = HeadingBlockRenderer.styledSpans(block.level(), block.headingSpans());
        y = SpanRenderer.measureSpanListFrom(ctx.font, styled, maxW, y,
                ctx.scale * SpanRenderer.headingScale(block.level()));
        y = HeadingBlockRenderer.afterHeadingY(y, block.level());
        if (!ctx.expandedKeys.contains(collapseTrackingKey(block.collapseKey()))) {
            y = ctx.measureBlockList(block.children(), y, maxW);
        }
        return y;
    }

    @Override
    public int render(RenderContext ctx, RichBlock.CollapsibleSection block, int x, int y, int maxW) {
        boolean collapsed = ctx.expandedKeys.contains(collapseTrackingKey(block.collapseKey()));
        int[] curY = { y };
        List<RichSpan> withArrow = new ArrayList<>(block.headingSpans().size() + 1);
        withArrow.add(new RichSpan.Text(collapsed ? "▸ " : "▾ ", Style.EMPTY));
        withArrow.addAll(block.headingSpans());
        HeadingBlockRenderer.renderHeadingLike(ctx, block.level(), withArrow, x, curY, maxW);
        if (curY[0] >= ctx.clipTop && y <= ctx.clipBot) {
            ctx.regions.add(new RichSpan.Region(x, y, x + maxW, curY[0],
                    new RichSpan.DetailsToggle(collapseTrackingKey(block.collapseKey()))));
        }
        if (!collapsed) {
            curY[0] = ctx.renderBlockList(block.children(), x, curY[0], maxW);
        }
        return curY[0];
    }

    @Override
    public int gapBefore(RichBlock.CollapsibleSection block) {
        return block.level() <= 2 ? SpanRenderer.GAP_HEADING_BEFORE : SpanRenderer.GAP_HEADING_BEFORE - 3;
    }

    public static String collapseTrackingKey(String collapseKey) {
        return "HCOL:" + collapseKey;
    }
}