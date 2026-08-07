package net.phoenixvine.wiki;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Entry point for Phoenix Wiki - a small, self-contained data-driven in-game wiki/markdown engine
 * meant to be jar-in-jar'd into other mods. It does not add any content of its own; host mods call
 * {@link net.phoenixvine.wiki.PhoenixWikiAPI} to open a wiki screen backed by their own
 * assets/&lt;namespace&gt;/wiki/&lt;locale&gt;/*.md pages.
 */
@Mod(PhoenixWiki.MOD_ID)
public class PhoenixWiki {

    public static final String MOD_ID = "phoenix_wiki";
    public static final Logger LOGGER = LogManager.getLogger("PhoenixWiki");

    public PhoenixWiki() {
        LOGGER.info("Phoenix Wiki loaded (library mod - see PhoenixWikiAPI to open a wiki screen)");
    }
}
