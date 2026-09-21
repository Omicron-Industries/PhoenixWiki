package net.phoenixvine.wiki.client.rich.render.blocks;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.render.BlockRenderer;
import net.phoenixvine.wiki.client.rich.render.RenderContext;
import net.phoenixvine.wiki.client.rich.render.SpanRenderer;

public final class ListItemBlockRenderer implements BlockRenderer<RichBlock.ListItem> {

    @Override
    public int measure(RenderContext ctx, RichBlock.ListItem block, int y, int maxW) {
        return SpanRenderer.measureSpanListFrom(ctx.font, block.spans(), maxW - block.indent(), y, ctx.scale);
    }

    @Override
    public int render(RenderContext ctx, RichBlock.ListItem block, int x, int y, int maxW) {
        int[] curY = { y };
        if (curY[0] >= ctx.clipTop && curY[0] + 8 <= ctx.clipBot) {
            ctx.g.drawString(ctx.font, block.marker(), x, curY[0], 0xFFAAAAAA, false);
        }
        SpanRenderer.renderSpanList(ctx.g, ctx.font, block.spans(),
                x + block.indent(), curY, x + block.indent(),
                maxW - block.indent(), ctx.clipTop, ctx.clipBot, ctx.regions, ctx.scale);
        return curY[0];
    }

    @Override
    public int gapBefore(RichBlock.ListItem block) {
        return SpanRenderer.GAP_LIST_ITEM;
    }
}
