package net.phoenixvine.wiki.client.rich.markdown.blocks;

import net.minecraft.resources.ResourceLocation;
import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.markdown.BlockParser;
import net.phoenixvine.wiki.client.rich.markdown.MarkdownPatterns;
import net.phoenixvine.wiki.client.rich.markdown.ParseContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

public final class ContainerBlockParser implements BlockParser {
    @Override
    public int tryParse(String[] lines, int i, List<RichBlock> out, ParseContext ctx) {
        Matcher open = MarkdownPatterns.CONTAINER_OPEN.matcher(lines[i].trim());
        if (!open.matches()) return -1;

        String type = open.group(1).toLowerCase();
        String title = open.group(2).trim();
        int depth = 1;
        int start = i + 1;
        int j = start;
        while (j < lines.length && depth > 0) {
            String t = lines[j].trim();
            if (MarkdownPatterns.CONTAINER_CLOSE.matcher(t).matches()) {
                depth--;
                if (depth == 0) break;
            } else if (MarkdownPatterns.CONTAINER_OPEN.matcher(t).matches()) {
                depth++;
            }
            j++;
        }
        String[] inner = Arrays.copyOfRange(lines, start, Math.min(j, lines.length));

        if (type.equals("hotspots")) {
            RichBlock.HotspotImage img = parseHotspotImage(title, inner);
            if (img != null) out.add(img);
            return j + 1;
        }

        List<RichBlock> children = ctx.parseLines(inner);
        if (type.equals("spoiler") || type.equals("details") || type.equals("loading")) {
            String key = (title.isEmpty() ? "section" : title) + "#" + start;
            out.add(new RichBlock.Details(key, title.isEmpty() ? "Details" : title, children,
                    type.equals("loading")));
        } else {
            out.add(new RichBlock.Callout(type, title, children));
        }
        return j + 1;
    }

    private static RichBlock.HotspotImage parseHotspotImage(String title, String[] inner) {
        String[] header = title.split(",", 3);
        if (header.length < 3) return null;
        ResourceLocation image = ResourceLocation.tryParse(header[0].trim());
        if (image == null) return null;
        int w = parseIntOr(header[1].trim(), -1);
        int h = parseIntOr(header[2].trim(), -1);
        if (w <= 0 || h <= 0) return null;

        List<RichBlock.Hotspot> spots = new ArrayList<>();
        for (String line : inner) {
            String t = line.trim();
            if (!t.startsWith("@")) continue;
            int sp = t.indexOf(' ');
            if (sp < 0) continue;
            String[] coord = t.substring(1, sp).split(",", 2);
            if (coord.length != 2) continue;
            int hx = parseIntOr(coord[0].trim(), Integer.MIN_VALUE);
            int hy = parseIntOr(coord[1].trim(), Integer.MIN_VALUE);
            if (hx == Integer.MIN_VALUE || hy == Integer.MIN_VALUE) continue;
            String tooltip = t.substring(sp + 1).trim();
            if (!tooltip.isEmpty()) spots.add(new RichBlock.Hotspot(hx, hy, tooltip));
        }
        return new RichBlock.HotspotImage(image, w, h, spots);
    }

    private static int parseIntOr(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
