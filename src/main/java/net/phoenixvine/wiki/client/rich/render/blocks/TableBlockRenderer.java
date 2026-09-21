package net.phoenixvine.wiki.client.rich.render.blocks;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.RichSpan;
import net.phoenixvine.wiki.client.rich.render.BlockRenderer;
import net.phoenixvine.wiki.client.rich.render.RenderContext;
import net.phoenixvine.wiki.client.rich.render.SpanRenderer;

import java.util.List;

public final class TableBlockRenderer implements BlockRenderer<RichBlock.Table> {

    @Override
    public int measure(RenderContext ctx, RichBlock.Table block, int y, int maxW) {
        int cols = block.header().size();
        for (List<List<RichSpan>> row : block.rows()) cols = Math.max(cols, row.size());
        int colW = cols > 0 ? Math.max(1, maxW / cols) : maxW;
        y += rowHeight(ctx.font, block.header(), colW, ctx.scale) + 1;
        for (List<List<RichSpan>> row : block.rows()) {
            y += rowHeight(ctx.font, row, colW, ctx.scale);
        }
        return y;
    }

    @Override
    public int render(RenderContext ctx, RichBlock.Table block, int x, int y, int maxW) {
        Font font = ctx.font;
        GuiGraphics g = ctx.g;
        int cols = block.header().size();
        for (List<List<RichSpan>> row : block.rows()) cols = Math.max(cols, row.size());
        if (cols == 0) return y;
        int colW = maxW / cols;

        int headerH = rowHeight(font, block.header(), colW, ctx.scale);
        if (y >= ctx.clipTop && y + headerH <= ctx.clipBot) {
            g.fill(x, y, x + maxW, y + headerH, 0xFF1E1830);
        }
        for (int c = 0; c < block.header().size(); c++) {
            int[] cellY = { y + 3 };
            SpanRenderer.renderSpanList(g, font, block.header().get(c), x + c * colW + 4, cellY, x + c * colW + 4,
                    colW - 8, ctx.clipTop, ctx.clipBot, ctx.regions, ctx.scale);
        }
        int rowY = y + headerH;
        if (rowY >= ctx.clipTop && rowY <= ctx.clipBot) {
            g.fill(x, rowY, x + maxW, rowY + 1, 0xFF3A3040);
        }
        rowY += 1;

        for (int r = 0; r < block.rows().size(); r++) {
            List<List<RichSpan>> row = block.rows().get(r);
            int rowH = rowHeight(font, row, colW, ctx.scale);
            if (r % 2 == 1 && rowY >= ctx.clipTop && rowY + rowH <= ctx.clipBot) {
                g.fill(x, rowY, x + maxW, rowY + rowH, 0x14FFFFFF);
            }
            for (int c = 0; c < row.size(); c++) {
                int[] cellY = { rowY + 3 };
                SpanRenderer.renderSpanList(g, font, row.get(c), x + c * colW + 4, cellY, x + c * colW + 4,
                        colW - 8, ctx.clipTop, ctx.clipBot, ctx.regions, ctx.scale);
            }
            rowY += rowH;
        }
        return rowY;
    }

    private static int rowHeight(Font font, List<List<RichSpan>> cells, int colW, float scale) {
        int maxH = Math.round(SpanRenderer.LINE_H * scale);
        for (List<RichSpan> cell : cells) {
            int h = SpanRenderer.measureSpanListFrom(font, cell, Math.max(1, colW - 8), 0, scale);
            maxH = Math.max(maxH, h);
        }
        return maxH + 6;
    }
}
