package net.phoenixvine.wiki;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.phoenixvine.wiki.client.screen.WikiScreen;
import net.phoenixvine.wiki.client.screen.WikiTheme;

public final class PhoenixWikiAPI {

    private PhoenixWikiAPI() {}

    /**
     * Holds onto the parent screen and opens the wiki linked to the namespace provided.
     * @param namespace falls back to wiki if left empty.
     */
    public static void open(Screen parent, String namespace) {
        open(parent, namespace, "wiki", WikiTheme.DEFAULT);
    }

    /**
     * Holds onto the parent screen, opens the wiki screen, and looks for the wiki files in a specified path.
     * @param namespace falls back to wiki if left empty.
     */
    public static void open(Screen parent, String namespace, String basePath) {
        open(parent, namespace, basePath, WikiTheme.DEFAULT);
    }

    /**
     * Holds onto the parent screen, opens the wiki screen, specifies a namespace, and sets a unique theme.
     * Usually gated behind a config.
     * @param namespace falls back to wiki if left empty.
     */
    public static void open(Screen parent, String namespace, String basePath, WikiTheme theme) {
        Minecraft.getInstance().setScreen(new WikiScreen(parent, namespace, basePath, theme));
    }

    /**
     * Makes a {@link WikiScreen} instance and holds it for later use.
     */
    public static Screen createScreen(Screen parent, String namespace, String basePath, WikiTheme theme) {
        return new WikiScreen(parent, namespace, basePath, theme);
    }
}
