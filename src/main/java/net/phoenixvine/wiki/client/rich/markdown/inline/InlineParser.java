package net.phoenixvine.wiki.client.rich.markdown.inline;

import net.phoenixvine.wiki.client.rich.RichSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class InlineParser {

    private InlineParser() {}

    public static List<RichSpan> parse(String input, Map<String, List<RichSpan.TipCandidate>> footnotes) {
        return parse(input, footnotes, InlineHandlerRegistry.DEFAULT);
    }

    public static List<RichSpan> parse(String input, Map<String, List<RichSpan.TipCandidate>> footnotes,
                                       InlineHandlerRegistry registry) {
        if (input == null || input.isEmpty()) return new ArrayList<>();

        var s = new InlineParseState(smartQuotes(input), footnotes);
        int len = s.length();
        int i = 0;
        while (i < len) {
            char c = s.charAt(i);
            int next = -1;
            for (InlineHandler h : registry.forTrigger(c)) {
                next = h.tryHandle(s, i);
                if (next >= 0) break;
            }
            if (next >= 0) {
                i = next;
            } else {
                s.buf.append(c);
                i++;
            }
        }
        s.flush();
        return s.out;
    }

    private static String smartQuotes(String input) {
        if (input.indexOf('"') < 0) return input;
        StringBuilder out = new StringBuilder(input.length());
        boolean inCode = false;
        boolean openNext = true;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '`') {
                inCode = !inCode;
                out.append(c);
            } else if (c == '"' && !inCode) {
                out.append(openNext ? '\u201C' : '\u201D');
                openNext = !openNext;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
