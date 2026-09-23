package net.phoenixvine.wiki.client.suite;

import com.mojang.blaze3d.systems.RenderSystem;
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
    public static final int PRIORITY_ARCHIVE = 60;
    public static final int PRIORITY_PHANTASIA = 70;

    private static final int BTN_SIZE = 20;
    private static final int GAP = 2;
    private static final int MARGIN = 4;
    private static final int GRID_ROWS = 3;

    // Other mods (FTB Library/Quests, etc.) sometimes draw their own HUD toggle in the same
    // top-left corner, and there's no API to ask any of them for their bounds -- so instead of
    // guessing which mods to avoid, the whole bar is just middle-click-draggable (see
    // onScreenMouseDrag below) and remembers wherever the player drops it. barX()/barY() expose
    // the live position so EMI's exclusion zone (see ChroniclesEmiPlugin) tracks it too.
    private static int leftMargin() {
        return MARGIN + SuiteHudConfig.getOffsetX();
    }

    private static int topMargin() {
        return MARGIN + SuiteHudConfig.getOffsetY();
    }

    public static int barX() {
        return leftMargin();
    }

    public static int barY() {
        return topMargin();
    }

    private record Entry(String modId, int priority, ResourceLocation icon, Supplier<Component> tooltip,
                         IntSupplier slotCount, Runnable onClick, int texWidth, int texHeight,
                         boolean tintToTheme) {}

    private static final List<Entry> ENTRIES = new ArrayList<>();

    private static final Object LOCK = new Object();

    public static void register(String modId, int priority, ResourceLocation icon, Component tooltip,
                                Runnable onClick) {
        register(modId, priority, icon, () -> tooltip, () -> 1, onClick);
    }

    public static void register(String modId, int priority, ResourceLocation icon, Supplier<Component> tooltip,
                                IntSupplier slotCount, Runnable onClick) {
        register(modId, priority, icon, tooltip, slotCount, onClick, 16, 16, false);
    }

    public static void register(String modId, int priority, ResourceLocation icon, Supplier<Component> tooltip,
                                IntSupplier slotCount, Runnable onClick, int texWidth, int texHeight,
                                boolean tintToTheme) {
        synchronized (LOCK) {
            ENTRIES.removeIf(e -> e.modId().equals(modId));
            ENTRIES.add(
                    new Entry(modId, priority, icon, tooltip, slotCount, onClick, texWidth, texHeight, tintToTheme));
            ENTRIES.sort(Comparator.comparingInt(Entry::priority));
        }
    }

    public static void unregister(String modId) {
        synchronized (LOCK) {
            ENTRIES.removeIf(e -> e.modId().equals(modId));
        }
    }

    public static Component getTooltip(String modId) {
        synchronized (LOCK) {
            return ENTRIES.stream().filter(e -> e.modId().equals(modId))
                    .findFirst().map(e -> e.tooltip().get()).orElse(null);
        }
    }

    public static java.util.List<String> getRegisteredModIds() {
        synchronized (LOCK) {
            return ENTRIES.stream().map(Entry::modId).toList();
        }
    }

    public static boolean isButtonEnabled(String modId) { return SuiteHudConfig.isEnabled(modId); }

    public static void setButtonEnabled(String modId, boolean enabled) { SuiteHudConfig.setEnabled(modId, enabled); }

    private record HudSlot(Entry entry, boolean isSettings, int x, int y, int size) {}

    private static int sizeFor(String modId) {
        return Math.round(BTN_SIZE * SuiteHudConfig.getEffectiveScale(modId));
    }

    private static List<HudSlot> computeLayout() {
        synchronized (LOCK) {
            List<String> ids = new ArrayList<>();
            List<Entry> owners = new ArrayList<>();
            List<Integer> sizes = new ArrayList<>();
            for (Entry e : ENTRIES) {
                if (!SuiteHudConfig.isEnabled(e.modId())) continue;
                int size = sizeFor(e.modId());
                int count = Math.max(0, e.slotCount().getAsInt());
                for (int i = 0; i < count; i++) {
                    ids.add(e.modId());
                    owners.add(e);
                    sizes.add(size);
                }
            }

            boolean showSettings = !ENTRIES.isEmpty();
            if (showSettings) {
                owners.add(null);
                sizes.add(sizeFor(SETTINGS_ID));
            }

            int n = sizes.size();
            List<HudSlot> slots = new ArrayList<>(n);
            if (n == 0) return slots;

            int cols = (n + GRID_ROWS - 1) / GRID_ROWS;
            int rows = Math.min(GRID_ROWS, n);
            int[] colW = new int[cols];
            int[] rowH = new int[rows];
            for (int idx = 0; idx < n; idx++) {
                int col = idx / GRID_ROWS, row = idx % GRID_ROWS;
                colW[col] = Math.max(colW[col], sizes.get(idx));
                rowH[row] = Math.max(rowH[row], sizes.get(idx));
            }

            int[] colX = new int[cols];
            int cx = leftMargin();
            for (int c = 0; c < cols; c++) {
                colX[c] = cx;
                cx += colW[c] + GAP;
            }
            int[] rowY = new int[rows];
            int cy = topMargin();
            for (int r = 0; r < rows; r++) {
                rowY[r] = cy;
                cy += rowH[r] + GAP;
            }

            for (int idx = 0; idx < n; idx++) {
                int col = idx / GRID_ROWS, row = idx % GRID_ROWS;
                int size = sizes.get(idx);
                int x = colX[col] + (colW[col] - size) / 2;
                int y = rowY[row] + (rowH[row] - size) / 2;
                Entry owner = owners.get(idx);
                slots.add(new HudSlot(owner, owner == null, x, y, size));
            }
            return slots;
        }
    }

    public static int barWidth() {
        List<HudSlot> slots = computeLayout();
        int maxRight = 0;
        for (HudSlot s : slots) maxRight = Math.max(maxRight, s.x() + s.size());
        return slots.isEmpty() ? 0 : maxRight + MARGIN;
    }

    public static int barHeight() {
        List<HudSlot> slots = computeLayout();
        int maxBottom = 0;
        for (HudSlot s : slots) maxBottom = Math.max(maxBottom, s.y() + s.size());
        return slots.isEmpty() ? 0 : maxBottom + MARGIN;
    }

    public static boolean screenWantsBar(Screen screen) {
        if (screen instanceof Aware) return false;
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return true;

        var mc = Minecraft.getInstance();
        if (mc.player == null) return true;

        Inventory playerInv = mc.player.getInventory();
        for (Slot slot : containerScreen.getMenu().slots) {
            if (slot.container == playerInv) return false;
        }
        return true;
    }

    private static HudSlot slotAt(List<HudSlot> slots, double mx, double my) {
        for (HudSlot s : slots) {
            if (mx >= s.x() && mx < s.x() + s.size() && my >= s.y() && my < s.y() + s.size()) return s;
        }
        return null;
    }

    /** The bar's own bounding box, independent of {@link #barWidth}/{@link #barHeight}'s
     *  "absolute right/bottom edge" semantics (those are sized for the EMI exclusion zone) --
     *  used so a middle-click anywhere between icons, not just exactly on one, starts a drag. */
    private static boolean withinBar(List<HudSlot> slots, double mx, double my) {
        if (slots.isEmpty()) return false;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = 0, maxY = 0;
        for (HudSlot s : slots) {
            minX = Math.min(minX, s.x());
            minY = Math.min(minY, s.y());
            maxX = Math.max(maxX, s.x() + s.size());
            maxY = Math.max(maxY, s.y() + s.size());
        }
        return mx >= minX && mx < maxX && my >= minY && my < maxY;
    }

    private static boolean dragging = false;
    private static double dragStartMx, dragStartMy;
    private static int dragStartOffsetX, dragStartOffsetY;

    private static void draw(GuiGraphics g, Minecraft mc, double hoverMx, double hoverMy) {
        var theme = PhoenixTheme.current();
        int panel = theme.panel.getColor();
        int border = theme.accent.getColor();

        List<HudSlot> slots = computeLayout();
        HudSlot hovered = slotAt(slots, hoverMx, hoverMy);

        for (HudSlot s : slots) {
            boolean isHovered = s == hovered;
            int x = s.x(), y = s.y(), size = s.size();
            g.fill(x, y, x + size, y + size, isHovered ? border : panel);
            g.fill(x, y, x + size, y + 1, border);
            g.fill(x, y, x + 1, y + size, border);
            g.fill(x + size - 1, y, x + size, y + size, border);
            g.fill(x, y + size - 1, x + size, y + size, border);
            if (s.isSettings()) {
                drawSettingsIcon(g, x, y, size, theme);
            } else {
                drawIcon(g, s.entry(), x, y, size, theme);
            }
        }

        if (hovered != null) {
            List<Component> tip = new ArrayList<>();
            if (hovered.isSettings()) {
                tip.add(Component.literal("§fSuite HUD Settings"));
                tip.add(Component.literal("§7Button scale, and which buttons show"));
            } else {
                tip.add(hovered.entry().tooltip().get());
                tip.add(Component.literal("§7Right-click to hide"));
            }
            g.renderComponentTooltip(mc.font, tip, (int) hoverMx, (int) hoverMy);
        }
    }

    private static void drawIcon(GuiGraphics g, Entry e, int x, int y, int size, PhoenixTheme t) {
        int pad = Math.max(1, Math.round(size * 0.1f));
        int iconSize = size - pad * 2;
        if (!e.tintToTheme()) {
            g.blit(e.icon(), x + pad, y + pad, iconSize, iconSize, 0, 0, 16, 16, e.texWidth(), e.texHeight());
            return;
        }

        int color = t.textDim.getColor();
        float a = ((color >>> 24) & 0xFF) / 255f;
       RenderSystem.setShaderColor(((color >> 16) & 0xFF) / 255f,
                ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, a > 0f ? a : 1f);
        try {
            g.blit(e.icon(), x + pad, y + pad, iconSize, iconSize, 0, 0, 16, 16, e.texWidth(), e.texHeight());
        } finally {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
    }

    private static void drawSettingsIcon(GuiGraphics g, int x, int y, int size, PhoenixTheme t) {
        int color = t.textDim.getColor() | 0xFF000000;
        int cx = x + size / 2, cy = y + size / 2;
        int r = Math.max(2, size / 2 - Math.max(2, size / 5));
        g.fill(cx - r, cy - 1, cx + r, cy + 1, color);
        g.fill(cx - 1, cy - r, cx + 1, cy + r, color);
        g.fill(cx - r + 1, cy - r + 1, cx + r - 1, cy - r + 2, color);
        g.fill(cx - r + 1, cy + r - 2, cx + r - 1, cy + r - 1, color);
        int inner = Math.max(1, r / 2);
        g.fill(cx - inner, cy - inner, cx + inner, cy + inner, color);
    }

    private static boolean entriesEmpty() {
        synchronized (LOCK) {
            return ENTRIES.isEmpty();
        }
    }

    private static final String SETTINGS_ID = "__suite_settings__";

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || entriesEmpty() || screenWantsBar(event.getScreen())) return;
        draw(event.getGuiGraphics(), mc, event.getMouseX(), event.getMouseY());
    }

    @SubscribeEvent
    public static void onScreenMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        var mc = Minecraft.getInstance();
        Screen screen = event.getScreen();
        if (mc.player == null || entriesEmpty() || screenWantsBar(screen)) return;

        List<HudSlot> slots = computeLayout();

        if (event.getButton() == 2 && withinBar(slots, event.getMouseX(), event.getMouseY())) {
            dragging = true;
            dragStartMx = event.getMouseX();
            dragStartMy = event.getMouseY();
            dragStartOffsetX = SuiteHudConfig.getOffsetX();
            dragStartOffsetY = SuiteHudConfig.getOffsetY();
            event.setCanceled(true);
            return;
        }

        HudSlot hit = slotAt(slots, event.getMouseX(), event.getMouseY());
        if (hit == null) return;

        event.setCanceled(true);
        if (hit.isSettings()) {
            if (event.getButton() == 0) mc.setScreen(new SuiteHudSettingsScreen(screen));
            return;
        }
        if (event.getButton() == 1) {

            SuiteHudConfig.setEnabled(hit.entry().modId(), false);
        } else if (event.getButton() == 0) {
            hit.entry().onClick().run();
        }
    }

    @SubscribeEvent
    public static void onScreenMouseDrag(ScreenEvent.MouseDragged.Pre event) {
        if (!dragging) return;
        event.setCanceled(true);

        Screen screen = event.getScreen();
        int newOffsetX = dragStartOffsetX + (int) Math.round(event.getMouseX() - dragStartMx);
        int newOffsetY = dragStartOffsetY + (int) Math.round(event.getMouseY() - dragStartMy);
        // Loose safety clamp -- keeps the drag handle from being dropped somewhere you can no
        // longer reach it, without needing to know the bar's exact size mid-drag.
        newOffsetX = Math.max(0, Math.min(screen.width - BTN_SIZE - MARGIN, newOffsetX));
        newOffsetY = Math.max(0, Math.min(screen.height - BTN_SIZE - MARGIN, newOffsetY));
        SuiteHudConfig.setOffsetLive(newOffsetX, newOffsetY);
    }

    @SubscribeEvent
    public static void onScreenMouseRelease(ScreenEvent.MouseButtonReleased.Pre event) {
        if (!dragging || event.getButton() != 2) return;
        dragging = false;
        SuiteHudConfig.commitOffset();
        event.setCanceled(true);
    }
}
