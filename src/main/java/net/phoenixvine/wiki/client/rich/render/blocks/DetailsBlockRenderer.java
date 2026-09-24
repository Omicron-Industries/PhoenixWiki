package net.phoenixvine.wiki.client.rich.render.blocks;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.RichSpan;
import net.phoenixvine.wiki.client.rich.render.BlockRenderer;
import net.phoenixvine.wiki.client.rich.render.FakeLoadingTimers;
import net.phoenixvine.wiki.client.rich.render.RenderContext;

public final class DetailsBlockRenderer implements BlockRenderer<RichBlock.Details> {

    private static final String LOADING_LABEL = "§8⚛ Consulting the reactor…";

    @Override
    public int measure(RenderContext ctx, RichBlock.Details block, int y, int maxW) {
        y += 14;
        if (ctx.expandedKeys.contains(block.expandKey())) {
            y += 3;
            if (isLoading(block)) {
                y += 10;
            } else {
                y = ctx.measureBlockList(block.children(), y, maxW - 10);
            }
            y += 3;
        }
        return y;
    }

    @Override
    public int render(RenderContext ctx, RichBlock.Details block, int x, int y, int maxW) {
        boolean expanded = ctx.expandedKeys.contains(block.expandKey());
        int headH = 14;

        if (y >= ctx.clipTop && y - headH <= ctx.clipBot) {
            ctx.g.fill(x, y, x + maxW, y + headH, 0xFF16121C);
            ctx.g.drawString(ctx.font, (expanded ? "§f▾ " : "§7▸ ") + "§l" + block.title(),
                    x + 4, y + 3, 0xFFE0D8F0, false);
            ctx.regions.add(new RichSpan.Region(x, y, x + maxW, y + headH,
                    new RichSpan.DetailsToggle(block.expandKey())));
        }
        int curY = y + headH + (expanded ? 3 : 0);

        if (expanded) {
            if (isLoading(block)) {
                if (curY >= ctx.clipTop && curY - 10 <= ctx.clipBot) {
                    ctx.g.drawString(ctx.font, LOADING_LABEL, x + 10, curY, 0xFF8888AA, false);
                }
                curY += 10;
            } else {
                curY = ctx.renderBlockList(block.children(), x + 10, curY, maxW - 10);
            }
            curY += 3;
        }
        return curY;
    }

    private static boolean isLoading(RichBlock.Details block) {
        return block.fakeLoading() && FakeLoadingTimers.isLoading(block.expandKey());
    }
}
