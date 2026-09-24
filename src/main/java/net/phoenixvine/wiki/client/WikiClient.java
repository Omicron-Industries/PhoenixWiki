package net.phoenixvine.wiki.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.phoenixvine.wiki.PhoenixWiki;
import net.phoenixvine.wiki.client.suite.SuiteHudConfig;
import net.phoenixvine.wiki.theme.PhoenixTheme;

@EventBusSubscriber(modid = PhoenixWiki.MOD_ID, value = Dist.CLIENT)
public class WikiClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            PhoenixTheme.loadThemes();
            SuiteHudConfig.init();
        });
    }

}