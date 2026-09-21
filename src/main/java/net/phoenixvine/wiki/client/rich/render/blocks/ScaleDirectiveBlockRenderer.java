package net.phoenixvine.wiki.client.rich.render.blocks;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.render.BlockRenderer;
import net.phoenixvine.wiki.client.rich.render.RenderContext;

public final class ScaleDirectiveBlockRenderer implements BlockRenderer<RichBlock.ScaleDirective> {

    @Override
    public int measure(RenderContext ctx, RichBlock.ScaleDirective block, int y, int maxW) {
        return y;
    }

    @Override
    public int render(RenderContext ctx, RichBlock.ScaleDirective block, int x, int y, int maxW) {
        return y;
    }

    @Override
    public int gapBefore(RichBlock.ScaleDirective block) {
        return 0;
    }
}
