package net.phoenixvine.wiki.client.rich.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.RichSpan;

import java.util.List;
import java.util.Set;

public final class RenderContext {

    public final GuiGraphics g;
    public final Font font;
    public final int clipTop;
    public final int clipBot;
    public final List<RichSpan.Region> regions;
    public final float scale;
    public final int accentColor;
    public final Set<String> expandedKeys;
    final BlockRendererRegistry registry;

    public RenderContext(GuiGraphics g, Font font, int clipTop, int clipBot, List<RichSpan.Region> regions,
                          float scale, int accentColor, Set<String> expandedKeys, BlockRendererRegistry registry) {
        this.g = g;
        this.font = font;
        this.clipTop = clipTop;
        this.clipBot = clipBot;
        this.regions = regions;
        this.scale = scale;
        this.accentColor = accentColor;
        this.expandedKeys = expandedKeys;
        this.registry = registry;
    }

    public RenderContext withAccent(int newAccent) {
        return new RenderContext(g, font, clipTop, clipBot, regions, scale, newAccent, expandedKeys, registry);
    }

    public int renderBlockList(List<RichBlock> blocks, int x, int y, int maxW) {
        return registry.renderAll(this, blocks, x, y, maxW);
    }

    public int measureBlockList(List<RichBlock> blocks, int y, int maxW) {
        return registry.measureAll(this, blocks, y, maxW);
    }
}
