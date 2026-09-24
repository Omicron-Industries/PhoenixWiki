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
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    public static int barX() {
        List<HudSlot> slots = computeLayout();
        if (slots.isEmpty()) return MARGIN;
        int minX = Integer.MAX_VALUE;
        for (HudSlot s : slots) minX = Math.min(minX, s.x());
        return minX;
    }

    public static int barY() {
        List<HudSlot> slots = computeLayout();
        if (slots.isEmpty()) return MARGIN;
        int minY = Integer.MAX_VALUE;
        for (HudSlot s : slots) minY = Math.min(minY, s.y());
        return minY;
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

    private record HudSlot(Entry entry, boolean isSettings, String key, int x, int y, int size) {}

    private static int sizeFor(String modId) {
        return Math.round(BTN_SIZE * SuiteHudConfig.getEffectiveScale(modId));
    }

    /** Puts every button back in its natural grid slot and forgets any in-flight drag/undo state
     *  tied to the positions being cleared. */
    public static void resetAllButtonPositions() {
        SuiteHudConfig.clearAllButtonAnchors();
        UNDO_STACK.clear();
        draggingKey = null;
        dragStartAnchor = null;
        physics.clear();
    }

    private static String keyFor(String modId, int index, int count) {
        return count > 1 ? modId + "#" + index : modId;
    }

    private static int clampToScreen(int v, int screenDim, int size) {
        return Math.max(0, Math.min(v, Math.max(0, screenDim - size)));
    }

    /** Converts an absolute top-left (x,y) into the {@code [anchorRight, distX, anchorBottom,
     *  distY]} form the config stores -- shared by drag-release and gravity-mode landing. */
    private static int[] toAnchor(int x, int y, int size, int screenW, int screenH) {
        boolean anchorRight = x + size / 2 > screenW / 2;
        boolean anchorBottom = y + size / 2 > screenH / 2;
        int distX = anchorRight ? screenW - x - size : x;
        int distY = anchorBottom ? screenH - y - size : y;
        return new int[] { anchorRight ? 1 : 0, distX, anchorBottom ? 1 : 0, distY };
    }

    private static List<HudSlot> computeLayout() {
        synchronized (LOCK) {
            List<String> keys = new ArrayList<>();
            List<Entry> owners = new ArrayList<>();
            List<Integer> sizes = new ArrayList<>();
            for (Entry e : ENTRIES) {
                if (!SuiteHudConfig.isEnabled(e.modId())) continue;
                int size = sizeFor(e.modId());
                int count = Math.max(0, e.slotCount().getAsInt());
                for (int i = 0; i < count; i++) {
                    keys.add(keyFor(e.modId(), i, count));
                    owners.add(e);
                    sizes.add(size);
                }
            }

            boolean showSettings = !ENTRIES.isEmpty();
            if (showSettings) {
                keys.add(SETTINGS_ID);
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
            int cx = MARGIN;
            for (int c = 0; c < cols; c++) {
                colX[c] = cx;
                cx += colW[c] + GAP;
            }
            int[] rowY = new int[rows];
            int cy = MARGIN;
            for (int r = 0; r < rows; r++) {
                rowY[r] = cy;
                cy += rowH[r] + GAP;
            }

            var window = Minecraft.getInstance().getWindow();
            int screenW = window.getGuiScaledWidth();
            int screenH = window.getGuiScaledHeight();

            // Pass 1: resolve every button NOT currently in flight (anchored, or its natural
            // grid slot). These double as the "floor" in-flight buttons can land/pile on.
            int[] restX = new int[n], restY = new int[n];
            boolean[] inFlight = new boolean[n];
            boolean[] resolved = new boolean[n];
            for (int idx = 0; idx < n; idx++) {
                String key = keys.get(idx);
                int size = sizes.get(idx);
                if (physics.containsKey(key)) {
                    inFlight[idx] = true;
                    continue;
                }
                int col = idx / GRID_ROWS, row = idx % GRID_ROWS;
                int[] anchor = SuiteHudConfig.getButtonAnchor(key);
                int x, y;
                if (anchor != null) {
                    x = anchor[0] == 1 ? screenW - anchor[1] - size : anchor[1];
                    y = anchor[2] == 1 ? screenH - anchor[3] - size : anchor[3];
                } else {
                    x = colX[col] + (colW[col] - size) / 2;
                    y = rowY[row] + (rowH[row] - size) / 2;
                }
                restX[idx] = clampToScreen(x, screenW, size);
                restY[idx] = clampToScreen(y, screenH, size);
                resolved[idx] = true;
            }

            // Pass 2: integrate flight for the rest, landing/piling on whatever's already
            // resolved -- including earlier buttons in this very loop, but NOT a not-yet-reached
            // in-flight sibling, whose restX/restY are still meaningless zeroes at this point
            // (that gap used to read as a phantom obstacle sitting at the top-left corner).
            for (int idx = 0; idx < n; idx++) {
                if (!inFlight[idx]) continue;
                String key = keys.get(idx);
                int size = sizes.get(idx);
                PhysicsState p = physics.get(key);

                boolean settled = tickPhysics(p, size, screenW, screenH, keys, restX, restY, sizes, resolved, idx);
                restX[idx] = clampToScreen((int) Math.round(p.x), screenW, size);
                restY[idx] = clampToScreen((int) Math.round(p.y), screenH, size);
                resolved[idx] = true;
                if (settled) {
                    physics.remove(key);
                    SuiteHudConfig.commitButtonAnchor(key, toAnchor(restX[idx], restY[idx], size, screenW, screenH));
                }
            }

            for (int idx = 0; idx < n; idx++) {
                Entry owner = owners.get(idx);
                slots.add(new HudSlot(owner, owner == null, keys.get(idx), restX[idx], restY[idx], sizes.get(idx)));
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

    /** EMI's recipe browser is a plain Screen (not a container), but EMI's sidebars show there, so we do too.
     *  Matched by name so there's no hard dependency on EMI. */
    private static final String EMI_RECIPE_SCREEN = "dev.emi.emi.screen.RecipeScreen";

    /** NOTE: despite the name, {@code true} means the bar is <b>skipped</b> on this screen. */
    public static boolean screenWantsBar(Screen screen) {
        if (screen instanceof Aware) return false;
        if (screen.getClass().getName().equals(EMI_RECIPE_SCREEN)) return false;
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return true;

        var mc = Minecraft.getInstance();
        if (mc.player == null) return true;

        Inventory playerInv = mc.player.getInventory();
        for (Slot slot : containerScreen.getMenu().slots) {
            if (slot.container == playerInv) return false;
        }
        return true;
    }

    /**
     * Screen-space rectangles ({@code x, y, width, height}, GUI-scaled) that recipe-viewer integrations (EMI)
     * should keep clear. Empty when the bar isn't shown on {@code screen}.
     *
     * <p>Buttons still sitting in their default grid are reported as <b>one</b> merged rectangle, not one per
     * button. EMI resolves each exclusion rect independently by cutting its whole sidebar panel to one side of
     * it, so 15 little rects make it shift the panel right past the first column of buttons even in the rows
     * below them. One rect gives it a single cut (panel sits under the grid, flush left). Buttons the user has
     * dragged elsewhere are reported individually, so they're still respected wherever they end up.
     */
    public static List<int[]> getButtonBounds(Screen screen) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || entriesEmpty() || screenWantsBar(screen)) return List.of();

        List<int[]> out = new ArrayList<>();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (HudSlot s : computeLayout()) {
            if (SuiteHudConfig.getButtonAnchor(s.key()) != null || physics.containsKey(s.key())) {
                out.add(new int[] { s.x(), s.y(), s.size(), s.size() });
            } else {
                minX = Math.min(minX, s.x());
                minY = Math.min(minY, s.y());
                maxX = Math.max(maxX, s.x() + s.size());
                maxY = Math.max(maxY, s.y() + s.size());
            }
        }
        if (minX != Integer.MAX_VALUE) out.add(0, new int[] { minX, minY, maxX - minX, maxY - minY });
        return out;
    }

    private static HudSlot slotAt(List<HudSlot> slots, double mx, double my) {
        for (HudSlot s : slots) {
            if (mx >= s.x() && mx < s.x() + s.size() && my >= s.y() && my < s.y() + s.size()) return s;
        }
        return null;
    }

    private static String draggingKey = null;
    private static int dragGrabX, dragGrabY;
    private static int dragCurX, dragCurY;
    @Nullable
    private static int[] dragStartAnchor;

    /** Recent (x, y, ms) drag samples, oldest first, trimmed to {@link #THROW_WINDOW_MS}. Throw
     *  velocity is measured across this whole window rather than just the last drag event --
     *  real flick gestures decelerate right before release, so sampling only the final delta
     *  captures the slowest instant of the motion instead of the actual throw. */
    private static final Deque<double[]> dragSamples = new ArrayDeque<>();
    private static final long THROW_WINDOW_MS = 120L;

    private static final int MAX_UNDO = 20;

    /** One entry per undoable action (a drag, or a hide) -- Ctrl+Z just runs whatever the most
     *  recent one captured, so both kinds share one chronological stack. */
    private record UndoEntry(Runnable undo) {}

    private static final Deque<UndoEntry> UNDO_STACK = new ArrayDeque<>();

    private static void pushUndo(Runnable undo) {
        if (UNDO_STACK.size() >= MAX_UNDO) UNDO_STACK.removeFirst();
        UNDO_STACK.addLast(new UndoEntry(undo));
    }

    private static boolean anchorEquals(int @Nullable [] a, int @Nullable [] b) {
        if (a == null || b == null) return a == b;
        return java.util.Arrays.equals(a, b);
    }

    // --- Gravity mode ------------------------------------------------------------------------

    /** A button in free flight: real velocity, gravity, wall/floor bounces, and piling on top of
     *  whatever's already settled underneath it. Mutable (updated in place every frame) rather
     *  than a record, since re-allocating one every tick for every falling/thrown button would
     *  otherwise churn a lot of garbage. */
    private static final class PhysicsState {

        double x, y, vx, vy;
        long startAtMs;
        long lastTickMs;
    }

    private static final Map<String, PhysicsState> physics = new ConcurrentHashMap<>();
    private static final long FALL_STAGGER_MS = 70L;

    private static final float GRAVITY_PX_S2 = 2600f;
    private static final float WALL_BOUNCE = 0.5f;
    private static final float FLOOR_BOUNCE = 0.35f;
    private static final float FLOOR_FRICTION = 0.80f;
    private static final float SETTLE_SPEED_PX_S = 60f;

    public static boolean isGravityMode() {
        return SuiteHudConfig.isGravityMode();
    }

    /** Flips gravity mode. Turning it on drops <i>every</i> currently-visible button from
     *  wherever it happens to be sitting right now -- including ones you'd previously dragged
     *  somewhere, like a cluster sitting on top of EMI's favorites bar -- into a pile at the
     *  bottom of the screen. Turning it off puts every gravity-dropped button back in the grid.
     *  While it's on, throwing a button (release a fast middle-click drag) launches it with real
     *  momentum instead of placing it instantly -- see {@link #onScreenMouseRelease}. */
    public static void setGravityMode(boolean enabled) {
        if (enabled == SuiteHudConfig.isGravityMode()) return;
        SuiteHudConfig.setGravityModeFlag(enabled);
        if (enabled) {
            dropToPile();
        } else {
            physics.clear();
            SuiteHudConfig.clearGravityPiles();
        }
    }

    private static void dropToPile() {
        List<HudSlot> slots = computeLayout();
        long now = System.currentTimeMillis();
        int order = 0;
        for (HudSlot s : slots) {
            PhysicsState p = new PhysicsState();
            p.x = s.x();
            p.y = s.y();
            p.startAtMs = now + order * FALL_STAGGER_MS;
            p.lastTickMs = p.startAtMs;
            physics.put(s.key(), p);
            SuiteHudConfig.markGravityPlaced(s.key());
            order++;
        }
    }

    /** Launches a button with real velocity -- used both by the pile-drop (v=0) and by throwing
     *  a button mid-drag (see {@link #onScreenMouseRelease}). */
    private static void launch(String key, double x, double y, double vx, double vy) {
        PhysicsState p = new PhysicsState();
        p.x = x;
        p.y = y;
        p.vx = vx;
        p.vy = vy;
        p.startAtMs = System.currentTimeMillis();
        p.lastTickMs = p.startAtMs;
        physics.put(key, p);
        SuiteHudConfig.markGravityPlaced(key);
    }

    /** Integrates one frame of {@code p}'s flight, bouncing off the screen edges and piling on
     *  top of any other slot ({@code restX}/{@code restY}, already-resolved this frame) it lands
     *  on. Returns true once it's settled (resting, slow enough to stop). Two buttons thrown at
     *  the same instant won't collide with each other mid-air -- only with what's already
     *  settled -- a deliberate scope cut rather than a full N-body simulation. */
    private static boolean tickPhysics(PhysicsState p, int size, int screenW, int screenH,
                                       List<String> keys, int[] restX, int[] restY, List<Integer> sizes,
                                       boolean[] resolved, int selfIdx) {
        long now = System.currentTimeMillis();
        if (now < p.startAtMs) return false;
        double dt = Math.min(0.05, Math.max(0.0, (now - p.lastTickMs) / 1000.0));
        p.lastTickMs = now;

        p.vy += GRAVITY_PX_S2 * dt;
        p.x += p.vx * dt;
        p.y += p.vy * dt;

        if (p.x < 0) {
            p.x = 0;
            p.vx = -p.vx * WALL_BOUNCE;
        }
        if (p.x + size > screenW) {
            p.x = screenW - size;
            p.vx = -p.vx * WALL_BOUNCE;
        }
        if (p.y < 0) {
            p.y = 0;
            p.vy = -p.vy * WALL_BOUNCE;
        }

        double floorY = screenH - size;
        for (int j = 0; j < keys.size(); j++) {
            if (j == selfIdx || !resolved[j]) continue;
            int otherSize = sizes.get(j);
            boolean overlapX = p.x + size > restX[j] && p.x < restX[j] + otherSize;
            if (overlapX) floorY = Math.min(floorY, restY[j] - size);
        }

        if (p.y >= floorY) {
            p.y = floorY;
            if (Math.abs(p.vy) > SETTLE_SPEED_PX_S) {
                p.vy = -p.vy * FLOOR_BOUNCE;
                p.vx *= FLOOR_FRICTION;
            } else {
                p.vy = 0;
                p.vx *= FLOOR_FRICTION;
                if (Math.abs(p.vx) < SETTLE_SPEED_PX_S * 0.5f) return true;
            }
        }
        return false;
    }

    // --- Phoenix rebirth + shy button ---------------------------------------------------------

    private static String justRebornModId = null;
    private static long justRebornAtMs = 0L;
    private static final long REBIRTH_FLOURISH_MS = 700L;

    private static String mercyModId = null;
    private static long mercyAtMs = 0L;
    private static final long MERCY_TOOLTIP_MS = 5000L;

    private static String lastHoveredKey = null;

    /** Every 3rd right-click hide is spared instead of applied -- "peeks back in" by just... not
     *  hiding, with a sad tooltip for a few seconds next time it's hovered. */
    private static void hideButton(String modId) {
        int count = SuiteHudConfig.incrementHideCount(modId);
        if (count % 3 == 0) {
            mercyModId = modId;
            mercyAtMs = System.currentTimeMillis();
            return;
        }
        SuiteHudConfig.setEnabled(modId, false);
        pushUndo(() -> {
            SuiteHudConfig.setEnabled(modId, true);
            justRebornModId = modId;
            justRebornAtMs = System.currentTimeMillis();
        });
    }


    private static void draw(GuiGraphics g, Minecraft mc, double hoverMx, double hoverMy) {
        var theme = PhoenixTheme.current();
        int panel = theme.panel.getColor();
        int border = theme.accent.getColor();

        List<HudSlot> slots = computeLayout();
        HudSlot hovered = slotAt(slots, hoverMx, hoverMy);

        if (justRebornModId != null && System.currentTimeMillis() - justRebornAtMs >= REBIRTH_FLOURISH_MS) {
            justRebornModId = null;
        }

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

            if (!s.isSettings() && s.entry().modId().equals(justRebornModId)) {
                float t = (System.currentTimeMillis() - justRebornAtMs) / (float) REBIRTH_FLOURISH_MS;
                int glowAlpha = (int) (200 * (1f - t)) & 0xFF;
                int glow = (glowAlpha << 24) | 0xFFDD66;
                int pad = -Math.round(3 * (1f - t));
                g.fill(x + pad, y + pad, x + size - pad, y + pad + 1, glow);
                g.fill(x + pad, y + size - pad - 1, x + size - pad, y + size - pad, glow);
                g.fill(x + pad, y + pad, x + pad + 1, y + size - pad, glow);
                g.fill(x + size - pad - 1, y + pad, x + size - pad, y + size - pad, glow);
            }
        }

        String hoveredKey = hovered != null ? hovered.key() : null;
        if (hoveredKey != null && !hoveredKey.equals(lastHoveredKey) && !hovered.isSettings()) {
            SuiteHudConfig.incrementHoverCount(hovered.entry().modId());
        }
        lastHoveredKey = hoveredKey;

        if (hovered != null) {
            List<Component> tip = new ArrayList<>();
            if (hovered.isSettings()) {
                tip.add(Component.literal("§fSuite HUD Settings"));
                tip.add(Component.literal("§7Button scale, and which buttons show"));
            } else if (hovered.entry().modId().equals(mercyModId) &&
                    System.currentTimeMillis() - mercyAtMs < MERCY_TOOLTIP_MS) {
                tip.add(Component.literal("§d🥺 You've tried to hide me 3 times."));
                tip.add(Component.literal("§7I'm staying, sorry."));
            } else {
                int count = SuiteHudConfig.getHoverCount(hovered.entry().modId());
                tip.add(Component.literal(count >= 25 ?
                        escalatedTooltipLine(count) : hovered.entry().tooltip().get().getString()));
                tip.add(Component.literal("§7Right-click to hide"));
            }
            g.renderComponentTooltip(mc.font, tip, (int) hoverMx, (int) hoverMy);
        }
    }

    private static String escalatedTooltipLine(int count) {
        if (count >= 400) return "§d§oYou've hovered this " + count + " times.";
        if (count >= 100) return "§d§oPlease, no more.";
        return "§d§oStill here.";
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

        HudSlot hit = slotAt(slots, event.getMouseX(), event.getMouseY());
        if (hit == null) return;

        if (event.getButton() == 2) {
            draggingKey = hit.key();
            dragGrabX = (int) Math.round(event.getMouseX()) - hit.x();
            dragGrabY = (int) Math.round(event.getMouseY()) - hit.y();
            dragCurX = hit.x();
            dragCurY = hit.y();
            dragSamples.clear();
            dragSamples.addLast(new double[] { hit.x(), hit.y(), System.currentTimeMillis() });
            if (physics.remove(draggingKey) != null) {
                SuiteHudConfig.unmarkGravityPlaced(draggingKey);
            }
            dragStartAnchor = SuiteHudConfig.getButtonAnchor(draggingKey);
            event.setCanceled(true);
            return;
        }

        event.setCanceled(true);
        if (hit.isSettings()) {
            if (event.getButton() == 0) mc.setScreen(new SuiteHudSettingsScreen(screen));
            return;
        }
        if (event.getButton() == 1) {

            hideButton(hit.entry().modId());
        } else if (event.getButton() == 0) {
            hit.entry().onClick().run();
        }
    }

    @SubscribeEvent
    public static void onScreenMouseDrag(ScreenEvent.MouseDragged.Pre event) {
        if (draggingKey == null) return;
        event.setCanceled(true);

        var window = Minecraft.getInstance().getWindow();
        int screenW = window.getGuiScaledWidth();
        int screenH = window.getGuiScaledHeight();
        int size = sizeForKey(draggingKey);

        int px = (int) Math.round(event.getMouseX()) - dragGrabX;
        int py = (int) Math.round(event.getMouseY()) - dragGrabY;
        dragCurX = px;
        dragCurY = py;

        long now = System.currentTimeMillis();
        dragSamples.addLast(new double[] { px, py, now });
        while (dragSamples.size() > 1 && now - dragSamples.peekFirst()[2] > THROW_WINDOW_MS) {
            dragSamples.removeFirst();
        }

        int[] a = toAnchor(px, py, size, screenW, screenH);
        SuiteHudConfig.setButtonAnchorLive(draggingKey, a[0] == 1, a[1], a[2] == 1, a[3]);
    }

    /** Throw velocity averaged over the last {@link #THROW_WINDOW_MS} of drag movement, so a
     *  natural end-of-flick deceleration doesn't zero out the throw. */
    private static double[] throwVelocity() {
        if (dragSamples.size() < 2) return new double[] { 0, 0 };
        double[] oldest = dragSamples.peekFirst();
        double[] newest = dragSamples.peekLast();
        double dt = (newest[2] - oldest[2]) / 1000.0;
        if (dt <= 0.001) return new double[] { 0, 0 };
        return new double[] { (newest[0] - oldest[0]) / dt, (newest[1] - oldest[1]) / dt };
    }

    private static int sizeForKey(String key) {
        int hashIdx = key.indexOf('#');
        return sizeFor(hashIdx < 0 ? key : key.substring(0, hashIdx));
    }

    @SubscribeEvent
    public static void onScreenMouseRelease(ScreenEvent.MouseButtonReleased.Pre event) {
        if (draggingKey == null || event.getButton() != 2) return;
        String key = draggingKey;
        int[] startAnchor = dragStartAnchor;
        double[] v = throwVelocity();
        int atX = dragCurX, atY = dragCurY;
        draggingKey = null;
        dragStartAnchor = null;

        if (SuiteHudConfig.isGravityMode()) {
            // Thrown: launches with velocity averaged over the recent drag window, instead of
            // snapping straight to an anchor -- see tickPhysics for the flight itself.
            launch(key, atX, atY, v[0], v[1]);
            pushUndo(() -> {
                physics.remove(key);
                SuiteHudConfig.commitButtonAnchor(key, startAnchor);
            });
        } else {
            int[] finalAnchor = SuiteHudConfig.getButtonAnchor(key);
            if (!anchorEquals(startAnchor, finalAnchor)) {
                pushUndo(() -> SuiteHudConfig.commitButtonAnchor(key, startAnchor));
            }
            SuiteHudConfig.commitButtonAnchor(key, finalAnchor);
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        var mc = Minecraft.getInstance();
        Screen screen = event.getScreen();
        if (mc.player == null || entriesEmpty() || screenWantsBar(screen)) return;
        if (event.getKeyCode() != GLFW.GLFW_KEY_Z || !Screen.hasControlDown()) return;
        if (UNDO_STACK.isEmpty()) return;

        UndoEntry last = UNDO_STACK.removeLast();
        last.undo().run();
        event.setCanceled(true);
    }
}