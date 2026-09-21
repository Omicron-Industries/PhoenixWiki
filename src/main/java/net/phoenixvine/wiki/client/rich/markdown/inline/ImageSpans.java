package net.phoenixvine.wiki.client.rich.markdown.inline;

import net.minecraft.resources.ResourceLocation;
import net.phoenixvine.wiki.client.rich.RichSpan;

import java.util.List;

public final class ImageSpans {
    private ImageSpans() {}

    public static void addImage(List<RichSpan> out, String rlPart) {
        int w = 48, h = 48;
        String[] parts = rlPart.split(",", 3);
        rlPart = parts[0].trim();
        if (parts.length >= 3) {
            try {
                w = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException ignored) {}
            try {
                h = Integer.parseInt(parts[2].trim());
            } catch (NumberFormatException ignored) {}
        }
        try {
            out.add(new RichSpan.Image(ResourceLocation.parse(rlPart), w, h));
        } catch (Exception ignored) {}
    }
}
