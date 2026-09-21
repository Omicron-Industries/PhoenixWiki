package net.phoenixvine.wiki.client.rich.markdown.blocks;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.RichSpan;
import net.phoenixvine.wiki.client.rich.markdown.BlockParser;
import net.phoenixvine.wiki.client.rich.markdown.MarkdownPatterns;
import net.phoenixvine.wiki.client.rich.markdown.ParseContext;

import java.util.ArrayList;
import java.util.List;

public final class TableBlockParser implements BlockParser {
    @Override
    public int tryParse(String[] lines, int i, List<RichBlock> out, ParseContext ctx) {
        if (!MarkdownPatterns.isTableStart(lines, i)) return -1;

        List<List<RichSpan>> header = new ArrayList<>();
        for (String cell : splitRow(lines[i])) header.add(ctx.parseInline(cell.trim()));
        int j = i + 2;

        List<List<List<RichSpan>>> rows = new ArrayList<>();
        while (j < lines.length) {
            String t = lines[j].trim();
            if (!t.contains("|") || !MarkdownPatterns.TABLE_ROW.matcher(t).matches()) break;
            List<List<RichSpan>> row = new ArrayList<>();
            for (String cell : splitRow(lines[j])) row.add(ctx.parseInline(cell.trim()));
            rows.add(row);
            j++;
        }

        out.add(new RichBlock.Table(header, rows));
        return j;
    }

    private static String[] splitRow(String line) {
        String t = line.trim();
        if (t.startsWith("|")) t = t.substring(1);
        if (t.endsWith("|")) t = t.substring(0, t.length() - 1);
        return t.split("\\|", -1);
    }
}
