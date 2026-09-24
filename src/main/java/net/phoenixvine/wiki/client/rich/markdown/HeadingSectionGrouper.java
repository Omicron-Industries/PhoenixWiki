package net.phoenixvine.wiki.client.rich.markdown;

import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.RichSpan;

import java.util.ArrayList;
import java.util.List;

public final class HeadingSectionGrouper {

    private HeadingSectionGrouper() {}

    public static List<RichBlock> group(List<RichBlock> blocks) {
        List<RichBlock> out = new ArrayList<>();
        int i = 0;
        while (i < blocks.size()) {
            RichBlock b = blocks.get(i);
            if (b instanceof RichBlock.Heading h && !h.collapsible()) {
                out.add(b);
                i++;
            } else if (b instanceof RichBlock.Heading h) {
                int j = i + 1;
                while (j < blocks.size()) {
                    RichBlock next = blocks.get(j);
                    if (next instanceof RichBlock.Heading nh && nh.level() <= h.level()) break;
                    j++;
                }
                List<RichBlock> children = group(new ArrayList<>(blocks.subList(i + 1, j)));
                String key = plainTextOf(h.spans()) + "#" + i;
                out.add(new RichBlock.CollapsibleSection(h.level(), h.spans(), key, children));
                i = j;
            } else if (b instanceof RichBlock.Callout c) {
                out.add(new RichBlock.Callout(c.type(), c.title(), group(c.children())));
                i++;
            } else if (b instanceof RichBlock.Details d) {
                out.add(new RichBlock.Details(d.expandKey(), d.title(), group(d.children()), d.fakeLoading()));
                i++;
            } else {
                out.add(b);
                i++;
            }
        }
        return out;
    }

    private static String plainTextOf(List<RichSpan> spans) {
        StringBuilder sb = new StringBuilder();
        for (RichSpan s : spans) {
            if (s instanceof RichSpan.Text t) sb.append(t.text());
        }
        return sb.toString();
    }
}
