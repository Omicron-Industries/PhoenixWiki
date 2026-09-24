package net.phoenixvine.wiki.integration;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import net.phoenixvine.wiki.client.suite.SuiteHudBar;

@EmiEntrypoint
public class PhoenixWikiEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.addGenericExclusionArea((screen, consumer) -> {
            for (int[] r : SuiteHudBar.getButtonBounds(screen)) {
                consumer.accept(new Bounds(r[0], r[1], r[2], r[3]));
            }
        });
    }
}
