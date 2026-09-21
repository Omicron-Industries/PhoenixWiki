package net.phoenixvine.wiki.client.rich.markdown;

import net.phoenixvine.wiki.client.rich.RichBlock;

import java.util.List;

public interface BlockParser {

    int tryParse(String[] lines, int i, List<RichBlock> out, ParseContext ctx);
}
