package net.phoenixvine.wiki.client.rich.markdown.inline;

import net.minecraft.network.chat.Style;
import net.phoenixvine.wiki.client.rich.RichSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Mutable state threaded through inline parsing: the running style/background/scale (toggled by
 *  handlers as they consume delimiters), the buffer of plain text not yet flushed into a span,
 *  and the output span list. */
public final class InlineParseState {

    public final String input;
    public final Map<String, List<RichSpan.TipCandidate>> footnotes;
    public final List<RichSpan> out = new ArrayList<>();
    public final StringBuilder buf = new StringBuilder();

    public Style style = Style.EMPTY;
    public int background = 0;
    public float scale = 1f;

    public InlineParseState(String input, Map<String, List<RichSpan.TipCandidate>> footnotes) {
        this.input = input;
        this.footnotes = footnotes;
    }

    public int length() {
        return input.length();
    }

    public char charAt(int i) {
        return input.charAt(i);
    }

    /** Turns any buffered plain text into a {@link RichSpan.Text} with the current style/background/scale. */
    public void flush() {
        if (buf.isEmpty()) return;
        out.add(new RichSpan.Text(buf.toString(), style, background, null, scale));
        buf.setLength(0);
    }
}
