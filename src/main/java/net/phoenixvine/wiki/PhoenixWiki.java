package net.phoenixvine.wiki;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PhoenixWiki.MOD_ID)
public class PhoenixWiki {

    public static final String MOD_ID = "phoenix_wiki";
    public static final Logger LOGGER = LogManager.getLogger("PhoenixWiki");

    public PhoenixWiki() {
        LOGGER.info("Phoenix Wiki loaded (library mod - see PhoenixWikiAPI to open a wiki screen)");

        // Eagerly load (and write back) the theme and suite-HUD configs on client setup, rather than
        // waiting for whichever consuming mod happens to touch them first -- SuiteHudConfig in
        // particular is otherwise only ever read from inside SuiteHudBar's own render/click handlers,
        // which don't run until a screen with the suite bar actually opens, so config/phoenix_wiki/
        // could otherwise never appear at all in a session that never opens one.
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus()
                .addListener((net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) -> {
                    net.phoenixvine.wiki.theme.PhoenixTheme.loadThemes();
                    net.phoenixvine.wiki.client.suite.SuiteHudConfig.init();
                }));
    }
}
