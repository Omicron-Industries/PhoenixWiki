package net.phoenixvine.wiki.client.rich.render.blocks;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.render.BlockRenderer;
import net.phoenixvine.wiki.client.rich.render.RenderContext;

public final class RuleBlockRenderer implements BlockRenderer<RichBlock.Rule> {

    @Override
    public int measure(RenderContext ctx, RichBlock.Rule block, int y, int maxW) {
        return y + 8;
    }

    @Override
    public int render(RenderContext ctx, RichBlock.Rule block, int x, int y, int maxW) {
        if (y + 1 >= ctx.clipTop && y <= ctx.clipBot) {
            ctx.g.fill(x, y + 3, x + maxW, y + 4, 0xFF3A3040);
        }
        return y + 8;
    }

    @Override
    public int gapBefore(RichBlock.Rule block) {
        return 0;
    }
}
