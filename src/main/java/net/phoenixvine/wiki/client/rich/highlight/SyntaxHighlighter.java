package net.phoenixvine.wiki.client.rich.highlight;

import net.minecraft.network.chat.Style;
import net.phoenixvine.wiki.client.rich.RichSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizes code-fence source into colored, optionally-interactive tokens.
 *
 * All language-specific behavior comes from {@link LanguageRegistry} - this class has no
 * per-language branching in it, which is the whole point: it doesn't change when a language
 * is added.
 *
 * Block comments ({@code /* ... *}{@code /}) and backtick template literals can span multiple
 * source lines, so tokenizing has to carry state ({@link Mode}) from one line to the next -
 * {@link #highlightDocument} is the entry point that does this correctly for a whole code block.
 * {@link #highlightLine} tokenizes a single line assuming it starts outside any multi-line
 * construct; keep using {@link #highlightDocument} for anything that might contain one.
 */
public final class SyntaxHighlighter {

    private SyntaxHighlighter() {}

    private static final int PLAIN_COLOR = 0xFFE0E0E0;
    private static final int COMMENT_COLOR = 0xFF6A9955;
    private static final int STRING_COLOR = 0xFFCE9178;
    private static final int ANNOTATION_COLOR = 0xFFDCDCAA;
    private static final int KEYWORD_COLOR = 0xFF569CD6;
    private static final int NUMBER_COLOR = 0xFFB5CEA8;
    private static final int LINK_COLOR = 0xFF55AAFF;
    private static final int TIP_COLOR = 0xFFAAFFAA;

    private static final Pattern CODE_ANNOTATION =
            Pattern.compile("\\[([^\\[\\]]+)]\\((wiki:[^()]+|tip:[^()]+|https?://[^()\\s]+)\\)");

    public record Token(String text, int color, RichSpan interactive) {
        public Token(String text, int color) {
            this(text, color, null);
        }
    }

    private enum Mode { NORMAL, BLOCK_COMMENT, TEMPLATE_LITERAL }

    private record LineResult(List<Token> tokens, Mode endMode) {}


    public static List<List<Token>> highlightDocument(String lang, String code) {
        List<List<Token>> result = new ArrayList<>();
        Mode mode = Mode.NORMAL;
        for (String rawLine : code.split("\n", -1)) {
            LineResult r = highlightLine(lang, rawLine, mode);
            result.add(r.tokens());
            mode = r.endMode();
        }
        return result;
    }


    public static List<Token> highlightLine(String lang, String line) {
        return highlightLine(lang, line, Mode.NORMAL).tokens();
    }


    private static LineResult highlightLine(String lang, String line, Mode modeIn) {
        List<Token> out = new ArrayList<>();
        Matcher m = CODE_ANNOTATION.matcher(line);
        int last = 0;
        Mode mode = modeIn;
        while (m.find()) {
            if (m.start() > last) {
                LineResult r = highlightPlain(lang, line.substring(last, m.start()), mode);
                out.addAll(r.tokens());
                mode = r.endMode();
            }
            String label = m.group(1);
            String target = m.group(2);
            if (target.startsWith("tip:")) {
                out.add(new Token(label, TIP_COLOR, new RichSpan.Tip(label, Style.EMPTY, target.substring(4))));
            } else {
                out.add(new Token(label, LINK_COLOR, new RichSpan.Link(label, Style.EMPTY, target)));
            }
            last = m.end();
        }
        if (last < line.length()) {
            LineResult r = highlightPlain(lang, line.substring(last), mode);
            out.addAll(r.tokens());
            mode = r.endMode();
        }
        return new LineResult(out, mode);
    }

    private static LineResult highlightPlain(String lang, String line, Mode modeIn) {
        List<Token> out = new ArrayList<>();
        var def = LanguageRegistry.forLang(lang);
        if (def.isEmpty()) {
            out.add(new Token(line, PLAIN_COLOR));
            return new LineResult(out, Mode.NORMAL);
        }

        var keywords = def.get().keywords();
        boolean templateLiterals = def.get().templateLiterals();
        boolean annotations = def.get().annotations();

        int i = 0, len = line.length();
        StringBuilder buf = new StringBuilder();
        Mode mode = modeIn;

        if (mode == Mode.BLOCK_COMMENT) {
            int end = line.indexOf("*/");
            if (end < 0) {
                out.add(new Token(line, COMMENT_COLOR));
                return new LineResult(out, Mode.BLOCK_COMMENT);
            }
            out.add(new Token(line.substring(0, end + 2), COMMENT_COLOR));
            i = end + 2;
            mode = Mode.NORMAL;
        } else if (mode == Mode.TEMPLATE_LITERAL) {
            int end = line.indexOf('`');
            if (end < 0) {
                out.add(new Token(line, STRING_COLOR));
                return new LineResult(out, Mode.TEMPLATE_LITERAL);
            }
            out.add(new Token(line.substring(0, end + 1), STRING_COLOR));
            i = end + 1;
            mode = Mode.NORMAL;
        }

        while (i < len) {
            char c = line.charAt(i);
            if (c == '/' && i + 1 < len && line.charAt(i + 1) == '/') {
                flushPlain(buf, out);
                out.add(new Token(line.substring(i), COMMENT_COLOR));
                i = len;
                break;
            }
            if (c == '/' && i + 1 < len && line.charAt(i + 1) == '*') {
                flushPlain(buf, out);
                int end = line.indexOf("*/", i + 2);
                if (end < 0) {
                    out.add(new Token(line.substring(i), COMMENT_COLOR));
                    return new LineResult(out, Mode.BLOCK_COMMENT);
                }
                out.add(new Token(line.substring(i, end + 2), COMMENT_COLOR));
                i = end + 2;
                continue;
            }
            if (c == '`' && templateLiterals) {
                flushPlain(buf, out);
                int end = line.indexOf('`', i + 1);
                if (end < 0) {
                    out.add(new Token(line.substring(i), STRING_COLOR));
                    return new LineResult(out, Mode.TEMPLATE_LITERAL);
                }
                out.add(new Token(line.substring(i, end + 1), STRING_COLOR));
                i = end + 1;
                continue;
            }
            if (c == '"' || c == '\'') {
                flushPlain(buf, out);
                int end = i + 1;
                while (end < len && line.charAt(end) != c) end++;
                end = Math.min(end + 1, len);
                out.add(new Token(line.substring(i, end), STRING_COLOR));
                i = end;
                continue;
            }
            if (c == '@' && annotations && i + 1 < len &&
                    (Character.isLetter(line.charAt(i + 1)) || line.charAt(i + 1) == '_')) {
                flushPlain(buf, out);
                int start = i;
                i++;
                while (i < len && (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '_')) i++;
                out.add(new Token(line.substring(start, i), ANNOTATION_COLOR));
                continue;
            }
            if (Character.isLetter(c) || c == '_') {
                int start = i;
                while (i < len && (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '_')) i++;
                String word = line.substring(start, i);
                if (keywords.contains(word)) {
                    flushPlain(buf, out);
                    out.add(new Token(word, KEYWORD_COLOR));
                } else {
                    buf.append(word);
                }
                continue;
            }
            if (Character.isDigit(c)) {
                int start = i;
                while (i < len && (Character.isDigit(line.charAt(i)) || line.charAt(i) == '.')) i++;
                flushPlain(buf, out);
                out.add(new Token(line.substring(start, i), NUMBER_COLOR));
                continue;
            }
            buf.append(c);
            i++;
        }
        flushPlain(buf, out);
        return new LineResult(out, Mode.NORMAL);
    }

    private static void flushPlain(StringBuilder buf, List<Token> out) {
        if (!buf.isEmpty()) {
            out.add(new Token(buf.toString(), PLAIN_COLOR));
            buf.setLength(0);
        }
    }
}