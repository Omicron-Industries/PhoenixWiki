package net.phoenixvine.wiki;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.phoenixvine.wiki.client.screen.WikiScreen;
import net.phoenixvine.wiki.client.screen.WikiTheme;

public final class PhoenixWikiAPI {

    private PhoenixWikiAPI() {}

    public static void open(Screen parent, String namespace) {
        open(parent, namespace, "wiki", WikiTheme.DEFAULT);
    }

    public static void open(Screen parent, String namespace, String basePath) {
        open(parent, namespace, basePath, WikiTheme.DEFAULT);
    }

    public static void open(Screen parent, String namespace, String basePath, WikiTheme theme) {
        Minecraft.getInstance().setScreen(new WikiScreen(parent, namespace, basePath, theme));
    }

    public static Screen createScreen(Screen parent, String namespace, String basePath, WikiTheme theme) {
        return new WikiScreen(parent, namespace, basePath, theme);
    }
}
