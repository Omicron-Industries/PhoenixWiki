package net.phoenixvine.wiki.client.rich;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * A single inline element within a block's text flow.
 *
 * Not sealed, for the same reason as {@link RichBlock}: a mod can add its own span type and
 * register a rendering path for it wherever it builds spans (an
 * {@link net.phoenixvine.wiki.client.rich.markdown.inline.InlineHandler} for markdown-driven
 * content, or directly if constructing {@link RichBlock}s by hand).
 */
public interface RichSpan {

    record Text(String text, Style style, int background, String copyText, float scale) implements RichSpan {
        public Text(String text, Style style) {
            this(text, style, 0, null, 1f);
        }

        public Text(String text, Style style, int background) {
            this(text, style, background, null, 1f);
        }

        public Text(String text, Style style, int background, String copyText) {
            this(text, style, background, copyText, 1f);
        }
    }

    record Link(String label, Style style, String url) implements RichSpan {}

    record Tip(String label, Style style, String tooltip) implements RichSpan {}

    /**
     * One candidate tooltip for a {@link ConditionalTip}: {@code conditionExpr} is a raw,
     * unparsed condition expression (mod-specific syntax) or {@code null}/blank for "always
     * matches" -- the engine never parses or evaluates it. A {@code null} tooltip means "no such
     * candidate," used as the caller-facing sentinel when nothing matches.
     */
    record TipCandidate(String conditionExpr, String tooltip) {}

    /**
     * A footnote reference with more than one possible tooltip, one per condition -- e.g.
     * {@code [^id]: text} and {@code [^id?condition]: text} definitions for the same id (see
     * {@link net.phoenixvine.wiki.client.rich.markdown.FootnoteExtractor}). The engine has no
     * condition system of its own and never resolves this itself: it renders as plain
     * non-interactive styled text unless the caller walks the block tree before render and
     * replaces each {@code ConditionalTip} with a concrete {@link Tip} (or {@link Text} if
     * nothing matches), evaluating {@link TipCandidate#conditionExpr()} against whatever
     * condition system that mod already has.
     */
    record ConditionalTip(String label, Style style, List<TipCandidate> candidates) implements RichSpan {}

    record Image(ResourceLocation texture, int w, int h) implements RichSpan {}

    record ItemIcon(ResourceLocation itemId, String tooltip) implements RichSpan {
        public ItemIcon(ResourceLocation itemId) {
            this(itemId, null);
        }
    }

    record CodeCopy(String code) implements RichSpan {}

    record DetailsToggle(String key) implements RichSpan {}

    record TocJump(int targetY) implements RichSpan {}

    record ChecklistToggle(String key, boolean checkedDefault) implements RichSpan {}

    record Region(int x1, int y1, int x2, int y2, RichSpan span) {

        public boolean contains(double mx, double my) {
            return mx >= x1 && mx < x2 && my >= y1 && my < y2;
        }
    }
}
