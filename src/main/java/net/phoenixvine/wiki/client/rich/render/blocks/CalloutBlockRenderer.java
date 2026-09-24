package net.phoenixvine.wiki.client.rich.render.blocks;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.render.BlockRenderer;
import net.phoenixvine.wiki.client.rich.render.RenderContext;

public final class CalloutBlockRenderer implements BlockRenderer<RichBlock.Callout> {

    @Override
    public int measure(RenderContext ctx, RichBlock.Callout block, int y, int maxW) {
        int inner = ctx.measureBlockList(block.children(), y + 12, maxW - 16);
        return inner + 6;
    }

    @Override
    public int render(RenderContext ctx, RichBlock.Callout block, int x, int y, int maxW) {
        int color = calloutColor(block.type());
        String icon = calloutIcon(block.type());
        String title = block.title().isEmpty() ? calloutTitle(block.type()) : block.title();

        int innerX = x + 10;
        int innerMaxW = maxW - 16;
        int headY = y + 6;
        int bodyY = ctx.measureBlockList(block.children(), headY + 12, innerMaxW);
        int boxH = (bodyY - y) + 6;

        if (y + boxH >= ctx.clipTop && y <= ctx.clipBot) {
            ctx.g.fill(x, y, x + maxW, y + boxH, (color & 0xFFFFFF) | 0x18000000);
            ctx.g.fill(x, y, x + 3, y + boxH, color);
            ctx.g.drawString(ctx.font, icon + " §l" + title, innerX, headY, color, false);
        }
        ctx.withAccent(color).renderBlockList(block.children(), innerX, headY + 12, innerMaxW);
        return y + boxH;
    }

    private static int calloutColor(String type) {
        return switch (type) {
            case "warning", "warn" -> 0xFFE0A030;
            case "danger", "error" -> 0xFFE05050;
            case "tip", "success" -> 0xFF50C878;
            case "note", "info" -> 0xFF55AAFF;
            case "skill_issue", "skillissue" -> 0xFFFF5577;
            case "cope", "copium" -> 0xFF8888AA;
            default -> 0xFF9966FF;
        };
    }

    private static String calloutIcon(String type) {
        return switch (type) {
            case "warning", "warn" -> "⚠";
            case "danger", "error" -> "⛔";
            case "tip", "success" -> "💡";
            case "note", "info" -> "ℹ";
            case "skill_issue", "skillissue" -> "🫵";
            case "cope", "copium" -> "😔";
            default -> "●";
        };
    }

    private static String calloutTitle(String type) {
        return switch (type) {
            case "skill_issue", "skillissue" -> "Skill Issue";
            case "cope", "copium" -> "Cope";
            default -> capitalize(type);
        };
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
