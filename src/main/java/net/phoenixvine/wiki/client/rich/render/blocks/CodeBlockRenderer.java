package net.phoenixvine.wiki.client.rich.render.blocks;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.RichSpan;
import net.phoenixvine.wiki.client.rich.highlight.SyntaxHighlighter;
import net.phoenixvine.wiki.client.rich.render.BlockRenderer;
import net.phoenixvine.wiki.client.rich.render.RenderContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders a fenced code block. Deliberately ignores {@link RenderContext#scale} - code blocks
 * always render at native size, matching the pre-split behavior.
 */
public final class CodeBlockRenderer implements BlockRenderer<RichBlock.CodeBlock> {

    @Override
    public int measure(RenderContext ctx, RichBlock.CodeBlock block, int y, int maxW) {
        return y + wrapCodeLines(ctx.font, block.lang(), block.code(), maxW).size() * 10 + 6;
    }

    @Override
    public int render(RenderContext ctx, RichBlock.CodeBlock block, int x, int y, int maxW) {
        GuiGraphics g = ctx.g;
        Font font = ctx.font;
        int clipTop = ctx.clipTop, clipBot = ctx.clipBot;
        List<RichSpan.Region> regions = ctx.regions;

        List<List<SyntaxHighlighter.Token>> visualLines = wrapCodeLines(font, block.lang(), block.code(), maxW);
        int lineH = 10;
        int boxH = visualLines.size() * lineH + 6;
        int btnW = font.width("⎘") + 8;

        if (y + boxH >= clipTop && y <= clipBot) {
            g.fill(x, y, x + maxW, y + boxH, 0xFF0A0A12);
            g.fill(x, y, x + 1, y + boxH, 0xFF3A3040);
            g.fill(x + maxW - btnW - 2, y + 1, x + maxW - 2, y + 1 + font.lineHeight + 2, 0x22FFFFFF);
            g.drawString(font, "§7⎘", x + maxW - btnW - 2 + 4, y + 3, 0xFFFFFFFF, false);
            for (int li = 0; li < visualLines.size(); li++) {
                int ly = y + 3 + li * lineH;
                int cx = x + 4;
                for (SyntaxHighlighter.Token tok : visualLines.get(li)) {
                    int tokW = font.width(tok.text());
                    if (ly >= clipTop && ly + 8 <= clipBot) {
                        g.drawString(font, tok.text(), cx, ly, tok.color(), false);
                        if (tok.interactive() != null) {
                            g.fill(cx, ly + 9, cx + tokW, ly + 10, tok.color());
                            regions.add(new RichSpan.Region(cx, ly, cx + tokW, ly + lineH, tok.interactive()));
                        }
                    }
                    cx += tokW;
                }
            }
            regions.add(new RichSpan.Region(x + maxW - btnW - 2, y + 1, x + maxW - 2, y + 1 + font.lineHeight + 2,
                    new RichSpan.CodeCopy(block.code())));
        }
        return y + boxH;
    }

    private static List<List<SyntaxHighlighter.Token>> wrapCodeLines(Font font, String lang, String code, int maxW) {
        int innerW = Math.max(8, maxW - 8);
        int btnReserve = font.width("⎘") + 8 + 10;
        int firstLineW = Math.max(8, innerW - btnReserve);
        List<List<SyntaxHighlighter.Token>> visualLines = new ArrayList<>();
        List<List<SyntaxHighlighter.Token>> perRawLine = SyntaxHighlighter.highlightDocument(lang, code);
        for (int r = 0; r < perRawLine.size(); r++) {
            int flw = (r == 0) ? firstLineW : -1;
            visualLines.addAll(wrapHighlightedLine(font, perRawLine.get(r), innerW, flw));
        }
        return visualLines;
    }

    private static List<List<SyntaxHighlighter.Token>> wrapHighlightedLine(Font font,
                                                                           List<SyntaxHighlighter.Token> tokens,
                                                                           int maxW, int firstLineMaxW) {
        List<List<SyntaxHighlighter.Token>> lines = new ArrayList<>();
        List<SyntaxHighlighter.Token> current = new ArrayList<>();
        int curW = 0;
        int curMaxW = firstLineMaxW > 0 ? firstLineMaxW : maxW;
        for (SyntaxHighlighter.Token tok : tokens) {
            String remaining = tok.text();
            while (!remaining.isEmpty()) {
                int w = font.width(remaining);
                if (curW + w <= curMaxW) {
                    current.add(new SyntaxHighlighter.Token(remaining, tok.color(), tok.interactive()));
                    curW += w;
                    remaining = "";
                } else if (curW == 0) {
                    int fitLen = Math.max(1, maxFitLength(font, remaining, curMaxW));
                    current.add(new SyntaxHighlighter.Token(remaining.substring(0, fitLen), tok.color(), tok.interactive()));
                    lines.add(current);
                    current = new ArrayList<>();
                    curW = 0;
                    curMaxW = maxW;
                    remaining = remaining.substring(fitLen);
                } else {
                    lines.add(current);
                    current = new ArrayList<>();
                    curW = 0;
                    curMaxW = maxW;
                }
            }
        }
        lines.add(current);
        return lines;
    }

    private static int maxFitLength(Font font, String text, int maxW) {
        int lo = 0, hi = text.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (font.width(text.substring(0, mid)) <= maxW) lo = mid; else hi = mid - 1;
        }
        return lo;
    }
}