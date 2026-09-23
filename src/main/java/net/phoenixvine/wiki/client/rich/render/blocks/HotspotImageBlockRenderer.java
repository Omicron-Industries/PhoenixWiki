package net.phoenixvine.wiki.client.rich.render.blocks;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.RichSpan;
import net.phoenixvine.wiki.client.rich.WikiRichTextRenderer;
import net.phoenixvine.wiki.client.rich.render.BlockRenderer;
import net.phoenixvine.wiki.client.rich.render.RenderContext;

public final class HotspotImageBlockRenderer implements BlockRenderer<RichBlock.HotspotImage> {

    @Override
    public int measure(RenderContext ctx, RichBlock.HotspotImage block, int y, int maxW) {
        return y + block.height();
    }

    @Override
    public int render(RenderContext ctx, RichBlock.HotspotImage block, int x, int y, int maxW) {
        if (y + block.height() >= ctx.clipTop && y <= ctx.clipBot) {
            ctx.g.blit(WikiRichTextRenderer.imageResolver.apply(block.image()), x, y, 0, 0,
                    block.width(), block.height(), block.width(), block.height());
            for (RichBlock.Hotspot spot : block.hotspots()) {
                int hx = x + spot.x();
                int hy = y + spot.y();
                ctx.g.fill(hx - 3, hy - 3, hx + 3, hy + 3, (0xAA << 24) | (ctx.accentColor & 0xFFFFFF));
                ctx.g.renderOutline(hx - 3, hy - 3, 6, 6, ctx.accentColor);
                ctx.regions.add(new RichSpan.Region(hx - 5, hy - 5, hx + 5, hy + 5,
                        new RichSpan.Tip(spot.tooltip(), net.minecraft.network.chat.Style.EMPTY, spot.tooltip())));
            }
        }
        return y + block.height();
    }
}
