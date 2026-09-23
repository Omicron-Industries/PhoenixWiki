package net.phoenixvine.wiki.client.rich.markdown;

import java.util.regex.Pattern;

public final class MarkdownPatterns {
    private MarkdownPatterns() {}

    public static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.*)$");
    public static final Pattern SCALE_DIRECTIVE = Pattern.compile("^\\{scale:(\\d+(?:\\.\\d+)?)}$");
    public static final Pattern UNORDERED = Pattern.compile("^(\\s*)[-*+]\\s+(.*)$");
    public static final Pattern CHECKBOX = Pattern.compile("^\\[([ xX])]\\s+(.*)$");
    public static final Pattern ORDERED = Pattern.compile("^(\\d+)\\.\\s+(.*)$");
    public static final Pattern RULE = Pattern.compile("^(-{3,}|\\*{3,}|_{3,})$");
    public static final Pattern FENCE = Pattern.compile("^```\\s*(\\S*)\\s*$");
    public static final Pattern QUOTE = Pattern.compile("^>\\s?(.*)$");
    public static final Pattern CONTAINER_OPEN = Pattern.compile("^:::(\\S+)\\s*(.*)$");
    public static final Pattern CONTAINER_CLOSE = Pattern.compile("^:::\\s*$");
    public static final Pattern TABLE_ROW = Pattern.compile("^\\|?.*\\|.*\\|?$");
    public static final Pattern TABLE_SEP = Pattern.compile("^\\|?[\\s:-]*-[\\s:-]*\\|[\\s:|-]*$");
    /** Group 1: id. Group 2: optional condition expression (raw, unparsed). Group 3: tooltip text. */
    public static final Pattern FOOTNOTE_DEF = Pattern.compile("^\\[\\^([^\\]?]+)(?:\\?([^\\]]+))?]:\\s*(.*)$");

    public static boolean isTableStart(String[] lines, int i) {
        if (i + 1 >= lines.length) return false;
        String row = lines[i].trim();
        String sep = lines[i + 1].trim();
        return row.contains("|") && TABLE_ROW.matcher(row).matches() && TABLE_SEP.matcher(sep).matches();
    }

    /**
     * Absorbs plain continuation lines (a wrapped line of the same list item, not a new item)
     * into {@code item}, stopping at whatever would start a new block -- so a soft line break
     * inside one list entry doesn't get misread as a separate item. Opt-in (see
     * {@link ParseContext#slurpListContinuations()}).
     */
    public static int slurpListContinuation(String[] lines, int i, StringBuilder item) {
        while (i < lines.length) {
            String t = lines[i].trim();
            if (t.isEmpty() || HEADING.matcher(t).matches() || UNORDERED.matcher(t).matches() ||
                    ORDERED.matcher(t).matches() || RULE.matcher(t).matches() || FENCE.matcher(t).matches() ||
                    QUOTE.matcher(t).matches() || CONTAINER_OPEN.matcher(t).matches() ||
                    CONTAINER_CLOSE.matcher(t).matches() || isTableStart(lines, i)) {
                break;
            }
            item.append(' ').append(t);
            i++;
        }
        return i;
    }
}
