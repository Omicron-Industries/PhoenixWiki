package net.phoenixvine.wiki;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.phoenixvine.wiki.client.suite.SuiteHudConfig;
import net.phoenixvine.wiki.theme.PhoenixTheme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PhoenixWiki.MOD_ID)
public class PhoenixWiki {

    public static final String MOD_ID = "phoenix_wiki";
    public static final Logger LOGGER = LogManager.getLogger("PhoenixWiki");

    public PhoenixWiki(FMLJavaModLoadingContext context) {
        LOGGER.info("Phoenix Wiki loaded (library mod. see PhoenixWikiAPI to open a wiki screen)");

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> context.getModEventBus()
                .addListener((FMLClientSetupEvent event) -> {
                    PhoenixTheme.loadThemes();
                    SuiteHudConfig.init();
                }));
    }
}
