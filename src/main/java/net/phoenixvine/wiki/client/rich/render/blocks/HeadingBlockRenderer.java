package net.phoenixvine.wiki.client.rich.render.blocks;

import net.minecraft.client.gui.GuiGraphics;
import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.RichSpan;
import net.phoenixvine.wiki.client.rich.render.BlockRenderer;
import net.phoenixvine.wiki.client.rich.render.RenderContext;
import net.phoenixvine.wiki.client.rich.render.SpanRenderer;

import java.util.List;

public final class HeadingBlockRenderer implements BlockRenderer<RichBlock.Heading> {

    private static final int BAR_OFFSET = 3;
    private static final int BAR_THICKNESS = 1;

    @Override
    public int measure(RenderContext ctx, RichBlock.Heading block, int y, int maxW) {
        List<RichSpan> styled = styledSpans(block.level(), block.spans());
        y = SpanRenderer.measureSpanListFrom(ctx.font, styled, maxW, y,
                ctx.scale * SpanRenderer.headingScale(block.level()));
        return afterHeadingY(y, block.level());
    }

    @Override
    public int render(RenderContext ctx, RichBlock.Heading block, int x, int y, int maxW) {
        int[] curY = { y };
        renderHeadingLike(ctx, block.level(), block.spans(), x, curY, maxW);
        return curY[0];
    }

    @Override
    public int gapBefore(RichBlock.Heading block) {
        return block.level() <= 2 ? SpanRenderer.GAP_HEADING_BEFORE : SpanRenderer.GAP_HEADING_BEFORE - 3;
    }

    public static void renderHeadingLike(RenderContext ctx, int level, List<RichSpan> spans, int x, int[] curY,
                                         int maxW) {
        GuiGraphics g = ctx.g;
        int headY = curY[0];
        List<RichSpan> styled = styledSpans(level, spans);
        float hScale = ctx.scale * SpanRenderer.headingScale(level);
        SpanRenderer.renderSpanList(g, ctx.font, styled, x, curY, x, maxW, ctx.clipTop, ctx.clipBot, ctx.regions,
                hScale);
        if (level <= 1) {
            int barY = curY[0] + BAR_OFFSET;
            if (barY + BAR_THICKNESS >= ctx.clipTop && headY <= ctx.clipBot) {
                g.fill(x, barY, x + maxW, barY + BAR_THICKNESS, ctx.accentColor);
            }
        }
        curY[0] = afterHeadingY(curY[0], level);
    }

    static int afterHeadingY(int textBottomY, int level) {
        int y = level <= 1 ? textBottomY + BAR_OFFSET + BAR_THICKNESS : textBottomY;
        return y + SpanRenderer.GAP_HEADING_AFTER;
    }

    static List<RichSpan> styledSpans(int level, List<RichSpan> spans) {
        return switch (Math.min(level, 3)) {
            case 1 -> SpanRenderer.withHeadingStyle(spans);
            case 2 -> SpanRenderer.withSubheadingStyle(spans);
            default -> SpanRenderer.withH3Style(spans);
        };
    }
}