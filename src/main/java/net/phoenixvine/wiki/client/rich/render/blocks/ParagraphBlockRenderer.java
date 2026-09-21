package net.phoenixvine.wiki.client.rich.render.blocks;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.render.BlockRenderer;
import net.phoenixvine.wiki.client.rich.render.RenderContext;
import net.phoenixvine.wiki.client.rich.render.SpanRenderer;

public final class ParagraphBlockRenderer implements BlockRenderer<RichBlock.Paragraph> {

    @Override
    public int measure(RenderContext ctx, RichBlock.Paragraph block, int y, int maxW) {
        return SpanRenderer.measureSpanListFrom(ctx.font, block.spans(), maxW, y, ctx.scale);
    }

    @Override
    public int render(RenderContext ctx, RichBlock.Paragraph block, int x, int y, int maxW) {
        int[] curY = { y };
        SpanRenderer.renderSpanList(ctx.g, ctx.font, block.spans(), x, curY, x, maxW, ctx.clipTop, ctx.clipBot,
                ctx.regions, ctx.scale);
        return curY[0];
    }
}
