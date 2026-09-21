package net.phoenixvine.wiki.client.rich.highlight;

import net.minecraft.network.chat.Style;
import net.phoenixvine.wiki.client.rich.RichSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SyntaxHighlighter {

    private SyntaxHighlighter() {}

    private static final int PLAIN_COLOR = 0xFFE0E0E0;
    private static final int COMMENT_COLOR = 0xFF6A9955;
    private static final int STRING_COLOR = 0xFFCE9178;
    private static final int ANNOTATION_COLOR = 0xFFDCDCAA;
    private static final int METHOD_COLOR = 0xFFDCDCAA;
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

    /** Tracks multi-line context (like block comments or template literals) across renderer lines. */
    public enum LexerState {
        NORMAL, IN_BLOCK_COMMENT, IN_TEMPLATE_LITERAL
    }

    public record HighlightResult(List<Token> tokens, LexerState endState) {}

    /** Backwards-compatible overload for one-off line parsing. */
    public static List<Token> highlightLine(String lang, String line) {
        return highlightLine(lang, line, LexerState.NORMAL).tokens();
    }

    public static HighlightResult highlightLine(String lang, String line, LexerState initialState) {
        List<Token> out = new ArrayList<>();
        Matcher m = CODE_ANNOTATION.matcher(line);
        int last = 0;
        LexerState currentState = initialState;

        while (m.find()) {
            if (m.start() > last) {
                var plainRes = highlightPlain(lang, line.substring(last, m.start()), currentState);
                out.addAll(plainRes.tokens());
                currentState = plainRes.endState();
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
            var plainRes = highlightPlain(lang, line.substring(last), currentState);
            out.addAll(plainRes.tokens());
            currentState = plainRes.endState();
        }

        return new HighlightResult(out, currentState);
    }

    private static HighlightResult highlightPlain(String lang, String line, LexerState initialState) {
        List<Token> out = new ArrayList<>();
        var def = LanguageRegistry.forLang(lang);
        if (def.isEmpty()) {
            out.add(new Token(line, PLAIN_COLOR));
            return new HighlightResult(out, initialState);
        }

        var keywords = def.get().keywords();
        boolean templateLiterals = def.get().templateLiterals();
        boolean annotations = def.get().annotations();

        int i = 0, len = line.length();
        StringBuilder buf = new StringBuilder();
        LexerState state = initialState;

        while (i < len) {

            if (state == LexerState.IN_BLOCK_COMMENT) {
                flushPlain(buf, out);
                int end = line.indexOf("*/", i);
                if (end != -1) {
                    out.add(new Token(line.substring(i, end + 2), COMMENT_COLOR));
                    i = end + 2;
                    state = LexerState.NORMAL;
                } else {
                    out.add(new Token(line.substring(i), COMMENT_COLOR));
                    return new HighlightResult(out, LexerState.IN_BLOCK_COMMENT);
                }
                continue;
            }

            // 2. Multi-line Template Literal State
            if (state == LexerState.IN_TEMPLATE_LITERAL) {
                flushPlain(buf, out);
                int end = i;
                while (end < len) {
                    if (line.charAt(end) == '\\' && end + 1 < len) {
                        end += 2;
                        continue;
                    }
                    if (line.charAt(end) == '`') {
                        end++;
                        out.add(new Token(line.substring(i, end), STRING_COLOR));
                        i = end;
                        state = LexerState.NORMAL;
                        break;
                    }
                    end++;
                }
                if (state == LexerState.IN_TEMPLATE_LITERAL) {
                    out.add(new Token(line.substring(i, end), STRING_COLOR));
                    return new HighlightResult(out, LexerState.IN_TEMPLATE_LITERAL);
                }
                continue;
            }

            char c = line.charAt(i);

            if (c == '/' && i + 1 < len) {
                char next = line.charAt(i + 1);
                if (next == '/') {
                    flushPlain(buf, out);
                    out.add(new Token(line.substring(i), COMMENT_COLOR));
                    break;
                } else if (next == '*') {
                    flushPlain(buf, out);
                    state = LexerState.IN_BLOCK_COMMENT;
                    int end = line.indexOf("*/", i + 2);
                    if (end != -1) {
                        out.add(new Token(line.substring(i, end + 2), COMMENT_COLOR));
                        i = end + 2;
                        state = LexerState.NORMAL;
                    } else {
                        out.add(new Token(line.substring(i), COMMENT_COLOR));
                        return new HighlightResult(out, LexerState.IN_BLOCK_COMMENT);
                    }
                    continue;
                }
            }

            if (c == '"' || c == '\'' || (c == '`' && templateLiterals)) {
                flushPlain(buf, out);
                int end = i + 1;
                boolean closed = false;
                while (end < len) {
                    if (line.charAt(end) == '\\' && end + 1 < len) {
                        end += 2;
                        continue;
                    }
                    if (line.charAt(end) == c) {
                        end++;
                        closed = true;
                        break;
                    }
                    end++;
                }
                out.add(new Token(line.substring(i, end), STRING_COLOR));
                i = end;

                if (!closed && c == '`') {
                    state = LexerState.IN_TEMPLATE_LITERAL;
                }
                continue;
            }

            if (c == '@' && annotations && i + 1 < len &&
                    (Character.isLetter(line.charAt(i + 1)) || line.charAt(i + 1) == '_')) {
                flushPlain(buf, out);
                int start = i;
                do i++;
                while (i < len && (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '_'));
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
                    int lookahead = i;
                    while (lookahead < len && Character.isWhitespace(line.charAt(lookahead))) lookahead++;

                    if (lookahead < len && line.charAt(lookahead) == '(') {
                        flushPlain(buf, out);
                        out.add(new Token(word, METHOD_COLOR));
                    } else {
                        buf.append(word);
                    }
                }
                continue;
            }

            if (Character.isDigit(c)) {
                int start = i;
                while (i < len && (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '.')) {
                    i++;
                }
                flushPlain(buf, out);
                out.add(new Token(line.substring(start, i), NUMBER_COLOR));
                continue;
            }

            buf.append(c);
            i++;
        }
        flushPlain(buf, out);
        return new HighlightResult(out, state);
    }

    private static void flushPlain(StringBuilder buf, List<Token> out) {
        if (!buf.isEmpty()) {
            out.add(new Token(buf.toString(), PLAIN_COLOR));
            buf.setLength(0);
        }
    }
}