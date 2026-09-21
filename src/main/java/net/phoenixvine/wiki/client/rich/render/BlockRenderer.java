package net.phoenixvine.wiki.client.rich.render;

import net.phoenixvine.wiki.client.rich.RichBlock;


public interface BlockRenderer<T extends RichBlock> {


    int measure(RenderContext ctx, T block, int y, int maxW);

    int render(RenderContext ctx, T block, int x, int y, int maxW);

    default int gapBefore(T block) {
        return SpanRenderer.GAP_PARAGRAPH;
    }
}
