package net.phoenixvine.wiki;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(PhoenixWiki.MOD_ID)
public class PhoenixWiki {

    public static final String MOD_ID = "phoenix_wiki";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PhoenixWiki(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Phoenix Wiki loaded (library mod. see PhoenixWikiAPI to open a wiki screen)");

    }
}