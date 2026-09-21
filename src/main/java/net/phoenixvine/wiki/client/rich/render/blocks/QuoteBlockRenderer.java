package net.phoenixvine.wiki.client.rich.render.blocks;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.render.BlockRenderer;
import net.phoenixvine.wiki.client.rich.render.RenderContext;
import net.phoenixvine.wiki.client.rich.render.SpanRenderer;

public final class QuoteBlockRenderer implements BlockRenderer<RichBlock.Quote> {

    @Override
    public int measure(RenderContext ctx, RichBlock.Quote block, int y, int maxW) {
        return SpanRenderer.measureSpanListFrom(ctx.font, block.spans(), maxW - 10, y, ctx.scale);
    }

    @Override
    public int render(RenderContext ctx, RichBlock.Quote block, int x, int y, int maxW) {
        int quoteY = y;
        int barX = x + 2;
        int textX = x + 10;
        int[] curY = { y };
        SpanRenderer.renderSpanList(ctx.g, ctx.font, block.spans(), textX, curY, textX, maxW - 10, ctx.clipTop,
                ctx.clipBot, ctx.regions, ctx.scale);
        if (curY[0] > quoteY && quoteY <= ctx.clipBot && curY[0] >= ctx.clipTop) {
            ctx.g.fill(barX, quoteY, barX + 2, curY[0] - 1, 0xFF5A5A6E);
        }
        return curY[0];
    }
}
