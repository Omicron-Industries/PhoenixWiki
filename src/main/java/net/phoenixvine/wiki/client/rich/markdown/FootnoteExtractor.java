package net.phoenixvine.wiki.client.rich.markdown;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public final class FootnoteExtractor {

    private FootnoteExtractor() {}

    public record Result(String[] lines, Map<String, String> footnotes) {}

    public static Result extract(String[] rawLines) {
        Map<String, String> footnotes = new LinkedHashMap<>();
        List<String> filtered = new ArrayList<>(rawLines.length);
        for (String line : rawLines) {
            Matcher fn = MarkdownPatterns.FOOTNOTE_DEF.matcher(line.trim());
            if (fn.matches()) {
                footnotes.put(fn.group(1), fn.group(2));
            } else {
                filtered.add(line);
            }
        }
        return new Result(filtered.toArray(String[]::new), footnotes);
    }
}
