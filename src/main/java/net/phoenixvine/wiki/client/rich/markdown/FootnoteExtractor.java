package net.phoenixvine.wiki.client.rich.markdown;

import net.phoenixvine.wiki.client.rich.RichSpan;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public final class FootnoteExtractor {

    private FootnoteExtractor() {}

    public record Result(String[] lines, Map<String, List<RichSpan.TipCandidate>> footnotes) {}

    public static Result extract(String[] rawLines) {
        Map<String, List<RichSpan.TipCandidate>> footnotes = new LinkedHashMap<>();
        List<String> filtered = new ArrayList<>(rawLines.length);
        for (String line : rawLines) {
            Matcher fn = MarkdownPatterns.FOOTNOTE_DEF.matcher(line.trim());
            if (fn.matches()) {
                footnotes.computeIfAbsent(fn.group(1), k -> new ArrayList<>())
                        .add(new RichSpan.TipCandidate(fn.group(2), fn.group(3)));
            } else {
                filtered.add(line);
            }
        }
        return new Result(filtered.toArray(String[]::new), footnotes);
    }
}
