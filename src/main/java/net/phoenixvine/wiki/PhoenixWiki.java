package net.phoenixvine.wiki;

import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PhoenixWiki.MOD_ID)
public class PhoenixWiki {

    public static final String MOD_ID = "phoenix_wiki";
    public static final Logger LOGGER = LogManager.getLogger("PhoenixWiki");

    public PhoenixWiki() {
        LOGGER.info("Phoenix Wiki loaded (library mod - see PhoenixWikiAPI to open a wiki screen)");
    }
}
