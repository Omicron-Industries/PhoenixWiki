package net.phoenixvine.wiki.client.suite;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenixvine.wiki.PhoenixWiki;
import net.phoenixvine.wiki.theme.PhoenixTheme;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = PhoenixWiki.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SuiteHudBar {

    private SuiteHudBar() {}

    public interface Aware {}

    public static final int PRIORITY_SOLARIS = 0;
    public static final int PRIORITY_ESSENTIALS = 10;
    public static final int PRIORITY_DOMAINS = 20;
    public static final int PRIORITY_CHRONICLES = 30;
    public static final int PRIORITY_GUILDS = 40;
    public static final int PRIORITY_EXCAVATE = 50;

    private static final int BTN_SIZE = 20;
    private static final int GAP = 2;
    private static final int MARGIN = 4;
    private static final int GRID_COLUMNS = 3;

    private record Entry(String modId, int priority, ResourceLocation icon, Supplier<Component> tooltip,
                         IntSupplier slotCount, Runnable onClick) {}

    private static final List<Entry> ENTRIES = new ArrayList<>();

    public static void register(String modId, int priority, ResourceLocation icon, Component tooltip,
                                Runnable onClick) {
        register(modId, priority, icon, () -> tooltip, () -> 1, onClick);
    }

    public static void register(String modId, int priority, ResourceLocation icon, Supplier<Component> tooltip,
                                IntSupplier slotCount, Runnable onClick) {
        ENTRIES.removeIf(e -> e.modId().equals(modId));
        ENTRIES.add(new Entry(modId, priority, icon, tooltip, slotCount, onClick));
        ENTRIES.sort(Comparator.comparingInt(Entry::priority));
    }

    public static void unregister(String modId) {
        ENTRIES.removeIf(e -> e.modId().equals(modId));
    }

    private static int totalSlotCount() {
        int count = 0;
        for (Entry e : ENTRIES) count += Math.max(0, e.slotCount().getAsInt());
        return count;
    }

    public static int barWidth() {
        int cols = Math.min(GRID_COLUMNS, Math.max(1, totalSlotCount()));
        return MARGIN * 2 + cols * BTN_SIZE + (cols - 1) * GAP;
    }

    public static int barHeight() {
        int rows = Math.max(1, (int) Math.ceil(totalSlotCount() / (double) GRID_COLUMNS));
        return MARGIN * 2 + rows * BTN_SIZE + (rows - 1) * GAP;
    }

    public static boolean screenWantsBar(Screen screen) {
        if (screen instanceof Aware) return true;
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return false;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;

        Inventory playerInv = mc.player.getInventory();
        for (Slot slot : containerScreen.getMenu().slots) {
            if (slot.container == playerInv) return true;
        }
        return false;
    }

    private static int slotX(int slotIndex) {
        return MARGIN + (slotIndex % GRID_COLUMNS) * (BTN_SIZE + GAP);
    }

    private static int slotY(int slotIndex) {
        return MARGIN + (slotIndex / GRID_COLUMNS) * (BTN_SIZE + GAP);
    }

    private static Entry entryAt(double mx, double my) {
        int idx = 0;
        for (Entry e : ENTRIES) {
            int slots = Math.max(0, e.slotCount().getAsInt());
            for (int i = 0; i < slots; i++) {
                int x = slotX(idx), y = slotY(idx);
                if (mx >= x && mx < x + BTN_SIZE && my >= y && my < y + BTN_SIZE) return e;
                idx++;
            }
        }
        return null;
    }

    private static void draw(GuiGraphics g, Minecraft mc, double hoverMx, double hoverMy) {
        PhoenixTheme t = PhoenixTheme.current();
        int panel = t.panel.getColor();
        int border = t.accent.getColor();

        int idx = 0;
        Entry hoveredEntry = null;
        for (Entry e : ENTRIES) {
            int slots = Math.max(0, e.slotCount().getAsInt());
            for (int i = 0; i < slots; i++) {
                int x = slotX(idx), y = slotY(idx);
                boolean hovered = hoverMx >= x && hoverMx < x + BTN_SIZE && hoverMy >= y && hoverMy < y + BTN_SIZE;
                if (hovered) hoveredEntry = e;

                g.fill(x, y, x + BTN_SIZE, y + BTN_SIZE, hovered ? border : panel);
                g.fill(x, y, x + BTN_SIZE, y + 1, border);
                g.fill(x, y, x + 1, y + BTN_SIZE, border);
                g.fill(x + BTN_SIZE - 1, y, x + BTN_SIZE, y + BTN_SIZE, border);
                g.fill(x, y + BTN_SIZE - 1, x + BTN_SIZE, y + BTN_SIZE, border);
                g.blit(e.icon(), x + 2, y + 2, 0, 0, 16, 16, 16, 128);
                idx++;
            }
        }

        if (hoveredEntry != null) {
            g.renderTooltip(mc.font, hoveredEntry.tooltip().get(), (int) hoverMx, (int) hoverMy);
        }
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || ENTRIES.isEmpty() || !screenWantsBar(event.getScreen())) return;
        draw(event.getGuiGraphics(), mc, event.getMouseX(), event.getMouseY());
    }

    @SubscribeEvent
    public static void onScreenMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != 0) return;
        Minecraft mc = Minecraft.getInstance();
        Screen screen = event.getScreen();
        if (mc.player == null || ENTRIES.isEmpty() || !screenWantsBar(screen)) return;

        Entry hit = entryAt(event.getMouseX(), event.getMouseY());
        if (hit == null) return;

        event.setCanceled(true);
        hit.onClick().run();
    }
}
