package net.phoenixvine.wiki.client.rich;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public sealed interface RichSpan
                                 permits RichSpan.Text, RichSpan.Link, RichSpan.Tip, RichSpan.Image,
                                 RichSpan.CodeCopy, RichSpan.ItemIcon, RichSpan.DetailsToggle, RichSpan.TocJump,
                                 RichSpan.ChecklistToggle {

    /** background is an ARGB int (0 = none) filled behind the text, e.g. for ==highlight==,
     * `code`, and &lt;kbd&gt; spans. copyText is non-null only for inline code spans - clicking
     * one copies it to the clipboard, same as a fenced code block's copy button. */
    record Text(String text, Style style, int background, String copyText) implements RichSpan {
        public Text(String text, Style style) {
            this(text, style, 0, null);
        }

        public Text(String text, Style style, int background) {
            this(text, style, background, null);
        }
    }

    record Link(String label, Style style, String url) implements RichSpan {}

    record Tip(String label, Style style, String tooltip) implements RichSpan {}

    record Image(ResourceLocation texture, int w, int h) implements RichSpan {}

    /** Renders the actual item icon (with vanilla item rendering, tint/glint included) for [item:id]. */
    record ItemIcon(ResourceLocation itemId) implements RichSpan {}

    /** Not rendered inline - used only as the payload of a click Region over a code block's copy button. */
    record CodeCopy(String code) implements RichSpan {}

    /** Not rendered inline - payload of a click Region over a collapsible section's header. */
    record DetailsToggle(String key) implements RichSpan {}

    /** Not rendered inline - payload of a click Region over an on-page table-of-contents entry. */
    record TocJump(int targetY) implements RichSpan {}

    /** Not rendered inline - payload of a click Region over a checklist item's checkbox glyph. */
    record ChecklistToggle(String key, boolean checkedDefault) implements RichSpan {}

    record Region(int x1, int y1, int x2, int y2, RichSpan span) {

        public boolean contains(double mx, double my) {
            return mx >= x1 && mx < x2 && my >= y1 && my < y2;
        }
    }
}
