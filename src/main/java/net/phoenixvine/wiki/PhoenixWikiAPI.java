package net.phoenixvine.wiki;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.phoenixvine.wiki.client.screen.WikiScreen;
import net.phoenixvine.wiki.client.screen.WikiTheme;

/**
 * Public API for host mods jar-in-jar-ing Phoenix Wiki. Call {@link #open} from a button/command/
 * keybind handler in your own mod - no registration or setup needed beyond having your markdown
 * pages under assets/&lt;your_mod_id&gt;/&lt;basePath&gt;/&lt;locale&gt;/*.md (basePath defaults to
 * "wiki" if you use the 2-arg overloads).
 *
 * <p>Client-side only - do not call from common/server code.
 */
public final class PhoenixWikiAPI {

    private PhoenixWikiAPI() {}

    /** Opens a wiki reading assets/&lt;namespace&gt;/wiki/&lt;locale&gt;/*.md, with the default theme. */
    public static void open(Screen parent, String namespace) {
        open(parent, namespace, "wiki", WikiTheme.DEFAULT);
    }

    /** Opens a wiki reading assets/&lt;namespace&gt;/&lt;basePath&gt;/&lt;locale&gt;/*.md, with the default theme. */
    public static void open(Screen parent, String namespace, String basePath) {
        open(parent, namespace, basePath, WikiTheme.DEFAULT);
    }

    /** Opens a wiki with a custom color theme matching your mod's own UI. */
    public static void open(Screen parent, String namespace, String basePath, WikiTheme theme) {
        Minecraft.getInstance().setScreen(new WikiScreen(parent, namespace, basePath, theme));
    }

    /** Builds a WikiScreen without immediately opening it, e.g. for use as a tab within your own screen. */
    public static Screen createScreen(Screen parent, String namespace, String basePath, WikiTheme theme) {
        return new WikiScreen(parent, namespace, basePath, theme);
    }
}
