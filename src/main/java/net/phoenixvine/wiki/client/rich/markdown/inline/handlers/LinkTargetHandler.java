package net.phoenixvine.wiki.client.rich.markdown.inline.handlers;

import net.phoenixvine.wiki.client.rich.RichSpan;
import net.phoenixvine.wiki.client.rich.markdown.inline.ImageSpans;
import net.phoenixvine.wiki.client.rich.markdown.inline.InlineHandler;
import net.phoenixvine.wiki.client.rich.markdown.inline.InlineParseState;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class LinkTargetHandler implements InlineHandler {

    /**
     * Extra recognized link-target scheme prefixes beyond the built-in {@code http://}/
     * {@code https://}/{@code wiki:} -- e.g. Archive registers {@code quest:} for its
     * Chronicles deep-link bridge. The engine itself never acts on these; it only decides
     * whether {@code [label](target)} becomes a clickable {@link RichSpan.Link} at all.
     */
    private static final Set<String> EXTRA_SCHEMES = ConcurrentHashMap.newKeySet();

    public static void registerScheme(String prefix) {
        EXTRA_SCHEMES.add(prefix);
    }

    @Override
    public char trigger() {
        return '[';
    }

    @Override
    public int tryHandle(InlineParseState s, int i) {
        String input = s.input;
        int len = input.length();
        int labelEnd = input.indexOf(']', i + 1);
        if (labelEnd <= i || labelEnd + 1 >= len || input.charAt(labelEnd + 1) != '(') return -1;
        int targetEnd = input.indexOf(')', labelEnd + 2);
        if (targetEnd <= labelEnd + 1) return -1;

        String label = input.substring(i + 1, labelEnd);
        String target = input.substring(labelEnd + 2, targetEnd);

        if (label.startsWith("img:")) {
            s.flush();
            ImageSpans.addImage(s.out, label.substring(4));
            return targetEnd + 1;
        }
        if (target.startsWith("http://") || target.startsWith("https://") || target.startsWith("wiki:") ||
                matchesExtraScheme(target)) {
            s.flush();
            s.out.add(new RichSpan.Link(label, s.style, target));
            return targetEnd + 1;
        }
        if (target.startsWith("tip:")) {
            s.flush();
            s.out.add(new RichSpan.Tip(label, s.style, target.substring(4)));
            return targetEnd + 1;
        }
        return -1;
    }

    private static boolean matchesExtraScheme(String target) {
        for (String scheme : EXTRA_SCHEMES) {
            if (target.startsWith(scheme)) return true;
        }
        return false;
    }
}
