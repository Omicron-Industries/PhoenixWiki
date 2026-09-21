package net.phoenixvine.wiki.client.rich.render.blocks;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.RichSpan;
import net.phoenixvine.wiki.client.rich.render.BlockRenderer;
import net.phoenixvine.wiki.client.rich.render.RenderContext;
import net.phoenixvine.wiki.client.rich.render.SpanRenderer;

import java.util.List;

public final class ChecklistBlockRenderer implements BlockRenderer<RichBlock.Checklist> {

    @Override
    public int measure(RenderContext ctx, RichBlock.Checklist block, int y, int maxW) {
        return SpanRenderer.measureSpanListFrom(ctx.font, block.spans(), maxW - block.indent(), y, ctx.scale);
    }

    @Override
    public int render(RenderContext ctx, RichBlock.Checklist block, int x, int y, int maxW) {
        boolean checked = ctx.expandedKeys.contains("CL1:" + block.checkKey())
                || !ctx.expandedKeys.contains("CL0:" + block.checkKey()) && block.checkedDefault();
        String glyph = checked ? "☑" : "☐";
        int[] curY = { y };
        if (curY[0] >= ctx.clipTop && curY[0] + 8 <= ctx.clipBot) {
            ctx.g.drawString(ctx.font, glyph, x, curY[0], checked ? 0xFF6FCF6F : 0xFFAAAAAA, false);
            ctx.regions.add(new RichSpan.Region(x, curY[0], x + block.indent(), curY[0] + 10,
                    new RichSpan.ChecklistToggle(block.checkKey(), block.checkedDefault())));
        }
        List<RichSpan> spans = checked ? SpanRenderer.withStrikethroughStyle(block.spans()) : block.spans();
        SpanRenderer.renderSpanList(ctx.g, ctx.font, spans, x + block.indent(), curY, x + block.indent(),
                maxW - block.indent(), ctx.clipTop, ctx.clipBot, ctx.regions, ctx.scale);
        return curY[0];
    }
}
