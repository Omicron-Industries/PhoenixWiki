package net.phoenixvine.wiki.client.rich;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.phoenixvine.wiki.client.rich.render.BlockRendererRegistry;
import net.phoenixvine.wiki.client.rich.render.RenderContext;
import net.phoenixvine.wiki.client.rich.render.SpanRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * Entry point for drawing and measuring a parsed wiki page. All public method signatures here
 * are unchanged from before the split - existing callers (e.g. WikiScreen) don't need to change.
 *
 * The actual per-block-type drawing logic lives in
 * {@code net.phoenixvine.wiki.client.rich.render}, one small {@link net.phoenixvine.wiki.client.rich.render.BlockRenderer}
 * class per {@link RichBlock} type, registered on {@link BlockRendererRegistry#DEFAULT}. To
 * support a new block type (including a custom one from another mod), write a
 * {@code BlockRenderer} for it and register it there - nothing in this class needs to change.
 * The generic word-wrap/click-region engine used by every block type lives in
 * {@link SpanRenderer}.
 */
public final class WikiRichTextRenderer {

    public static final int LINE_H = SpanRenderer.LINE_H;
    public static final float DEFAULT_SCALE = SpanRenderer.DEFAULT_SCALE;

    public static UnaryOperator<ResourceLocation> imageResolver = UnaryOperator.identity();

    private WikiRichTextRenderer() {}

    /**
     * Chains a resolver onto whatever's already registered, instead of clobbering it -- lets more
     * than one mod contribute a resolver (each decides whether to transform a given
     * {@link ResourceLocation} or pass it through to the previous one unchanged). Prefer this over
     * assigning {@link #imageResolver} directly.
     */
    public static void registerImageResolver(UnaryOperator<ResourceLocation> resolver) {
        UnaryOperator<ResourceLocation> previous = imageResolver;
        imageResolver = loc -> resolver.apply(previous.apply(loc));
    }

    public static List<RichSpan.Region> render(GuiGraphics g, Font font,
                                               List<RichSpan> spans,
                                               int x, int y, int maxW,
                                               int scrollY, int clipTop, int clipBot) {
        return render(g, font, spans, x, y, maxW, scrollY, clipTop, clipBot, 1.0f);
    }

    public static List<RichSpan.Region> render(GuiGraphics g, Font font,
                                               List<RichSpan> spans,
                                               int x, int y, int maxW,
                                               int scrollY, int clipTop, int clipBot, float scale) {
        List<RichSpan.Region> regions = new ArrayList<>();
        int[] endY = { y - scrollY };
        SpanRenderer.renderSpanList(g, font, spans, x, endY, x, maxW, clipTop, clipBot, regions, scale);
        return regions;
    }

    public static int measureHeight(Font font, List<RichSpan> spans, int maxW) {
        return measureHeight(font, spans, maxW, 1.0f);
    }

    private static final int HEIGHT_CACHE_SLOTS = 6;
    private static final List<RichSpan>[] hcSpans = new List[HEIGHT_CACHE_SLOTS];
    private static final int[] hcWidth = new int[HEIGHT_CACHE_SLOTS];
    private static final float[] hcScale = new float[HEIGHT_CACHE_SLOTS];
    private static final int[] hcHeight = new int[HEIGHT_CACHE_SLOTS];
    private static int hcNext = 0;

    public static int measureHeight(Font font, List<RichSpan> spans, int maxW, float scale) {
        for (int i = 0; i < HEIGHT_CACHE_SLOTS; i++) {
            if (hcSpans[i] == spans && hcWidth[i] == maxW && hcScale[i] == scale) {
                return hcHeight[i];
            }
        }
        int height = SpanRenderer.measureSpanList(font, spans, maxW, scale);
        hcSpans[hcNext] = spans;
        hcWidth[hcNext] = maxW;
        hcScale[hcNext] = scale;
        hcHeight[hcNext] = height;
        hcNext = (hcNext + 1) % HEIGHT_CACHE_SLOTS;
        return height;
    }

    public static List<RichSpan.Region> renderBlocks(GuiGraphics g, Font font,
                                                     List<RichBlock> blocks,
                                                     int x, int y, int maxW,
                                                     int scrollY, int clipTop, int clipBot) {
        return renderBlocks(g, font, blocks, x, y, maxW, scrollY, clipTop, clipBot, 1.0f,
                SpanRenderer.DEFAULT_ACCENT, Set.of());
    }

    public static List<RichSpan.Region> renderBlocks(GuiGraphics g, Font font,
                                                     List<RichBlock> blocks,
                                                     int x, int y, int maxW,
                                                     int scrollY, int clipTop, int clipBot, int accentColor) {
        return renderBlocks(g, font, blocks, x, y, maxW, scrollY, clipTop, clipBot, 1.0f,
                accentColor, Set.of());
    }

    public static List<RichSpan.Region> renderBlocks(GuiGraphics g, Font font,
                                                     List<RichBlock> blocks,
                                                     int x, int y, int maxW,
                                                     int scrollY, int clipTop, int clipBot, int accentColor,
                                                     Set<String> expandedKeys) {
        return renderBlocks(g, font, blocks, x, y, maxW, scrollY, clipTop, clipBot, 1.0f,
                accentColor, expandedKeys);
    }

    public static List<RichSpan.Region> renderBlocks(GuiGraphics g, Font font,
                                                     List<RichBlock> blocks,
                                                     int x, int y, int maxW,
                                                     int scrollY, int clipTop, int clipBot, float scale,
                                                     int accentColor, Set<String> expandedKeys) {
        List<RichSpan.Region> regions = new ArrayList<>();
        RenderContext ctx = new RenderContext(g, font, clipTop, clipBot, regions, scale, accentColor, expandedKeys,
                BlockRendererRegistry.DEFAULT);
        ctx.renderBlockList(blocks, x, y - scrollY, maxW);
        return regions;
    }

    public static int measureBlocksHeight(Font font, List<RichBlock> blocks, int maxW) {
        return measureBlocksHeight(font, blocks, maxW, 1.0f, Set.of());
    }

    public static int measureBlocksHeight(Font font, List<RichBlock> blocks, int maxW, Set<String> expandedKeys) {
        return measureBlocksHeight(font, blocks, maxW, 1.0f, expandedKeys);
    }

    public static int measureBlocksHeight(Font font, List<RichBlock> blocks, int maxW, float scale,
                                          Set<String> expandedKeys) {
        RenderContext ctx = new RenderContext(null, font, 0, 0, List.of(), scale, SpanRenderer.DEFAULT_ACCENT,
                expandedKeys, BlockRendererRegistry.DEFAULT);
        return ctx.measureBlockList(blocks, 0, maxW);
    }

    public record HeadingInfo(int level, String text, int y) {}

    public static List<HeadingInfo> computeHeadingOffsets(Font font, List<RichBlock> blocks, int maxW) {
        return computeHeadingOffsets(font, blocks, maxW, 1.0f);
    }

    public static List<HeadingInfo> computeHeadingOffsets(Font font, List<RichBlock> blocks, int maxW, float scale) {
        List<HeadingInfo> out = new ArrayList<>();
        RenderContext ctx = new RenderContext(null, font, 0, 0, List.of(), scale, SpanRenderer.DEFAULT_ACCENT,
                Set.of(), BlockRendererRegistry.DEFAULT);
        computeHeadingOffsetsInto(ctx, blocks, maxW, new int[] { 0 }, true, out);
        return out;
    }

    private static void computeHeadingOffsetsInto(RenderContext ctx, List<RichBlock> blocks, int maxW, int[] y,
                                                  boolean first, List<HeadingInfo> out) {
        for (RichBlock block : blocks) {
            if (block instanceof RichBlock.Blank) {
                y[0] += SpanRenderer.GAP_BLANK;
                first = false;
                continue;
            }
            if (!first) y[0] += BlockRendererRegistry.DEFAULT.gapBeforeFor(block);
            first = false;
            if (block instanceof RichBlock.Heading h) {
                out.add(new HeadingInfo(h.level(), SpanRenderer.plainText(h.spans()), y[0]));
                y[0] = BlockRendererRegistry.DEFAULT.measureOne(ctx, block, y[0], maxW);
            } else if (block instanceof RichBlock.CollapsibleSection s) {
                out.add(new HeadingInfo(s.level(), SpanRenderer.plainText(s.headingSpans()), y[0]));
                y[0] = BlockRendererRegistry.DEFAULT.measureOne(ctx, new RichBlock.Heading(s.level(), s.headingSpans()),
                        y[0], maxW);
                computeHeadingOffsetsInto(ctx, s.children(), maxW, y, true, out);
            } else {
                y[0] = BlockRendererRegistry.DEFAULT.measureOne(ctx, block, y[0], maxW);
            }
        }
    }
}
