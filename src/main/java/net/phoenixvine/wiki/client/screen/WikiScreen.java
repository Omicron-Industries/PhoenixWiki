package net.phoenixvine.wiki.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.phoenixvine.wiki.client.rich.RichBlock;
import net.phoenixvine.wiki.client.rich.RichSpan;
import net.phoenixvine.wiki.client.rich.WikiMarkdownParser;
import net.phoenixvine.wiki.client.rich.WikiRichTextRenderer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

public class WikiScreen extends Screen {

    private static final Logger LOGGER = LogManager.getLogger("PhoenixWiki");

    private static final int HEADER_H = 28;
    private static final int FOOTER_H = 28;
    private static final int MIN_SIDEBAR_W = 80;
    private static final int MAX_SIDEBAR_W = 260;
    private static final int DEFAULT_SIDEBAR_W = 140;
    private static final int HANDLE_W = 4;
    private static final int MARGIN = 10;
    private static final int SEARCH_H = 14;
    private static final int LIST_TOP_OFFSET = SEARCH_H + 6;
    private static final int TOC_W = 130;

    private static int savedSidebarW = -1;

    private final Screen parent;
    private final String namespace;
    private final String basePath;
    private final WikiTheme theme;

    private List<WikiPageLoader.Page> pages = List.of();
    private List<Integer> visibleIndices = List.of();
    private final Map<String, List<RichBlock>> parsedCache = new HashMap<>();
    private int activePage = 0;
    private int scrollY = 0;
    private int cachedContentH = 0;
    private List<RichSpan.Region> richRegions = List.of();
    private List<RichSpan.Region> tocRegions = List.of();
    private String justCopiedCode = null;
    private long justCopiedAtMs = 0L;
    private static final long COPY_FLASH_MS = 1400L;
    private int sidebarW = DEFAULT_SIDEBAR_W;
    private boolean draggingSidebar = false;
    private final Set<String> expandedKeys = new HashSet<>();
    private final Set<String> collapsedChapters = new HashSet<>();
    private boolean tocOpen = true;
    private EditBox searchBox;

    private record SidebarEntry(boolean isHeader, String chapter, int pageIndex) {}

    public WikiScreen(Screen parent, String namespace, String basePath) {
        this(parent, namespace, basePath, WikiTheme.DEFAULT);
    }

    public WikiScreen(Screen parent, String namespace, String basePath, WikiTheme theme) {
        super(Component.literal("Wiki"));
        this.parent = parent;
        this.namespace = namespace;
        this.basePath = basePath;
        this.theme = theme;
    }

    private List<SidebarEntry> buildSidebarEntries() {
        List<SidebarEntry> out = new ArrayList<>();
        String lastChapter = " ";
        for (int row = 0; row < visibleIndices.size(); row++) {
            int i = visibleIndices.get(row);
            String chapter = pages.get(i).chapter();
            String key = chapter == null ? "" : chapter;
            if (!key.equals(lastChapter)) {
                lastChapter = key;
                if (chapter != null) out.add(new SidebarEntry(true, chapter, -1));
            }
            if (chapter == null || !collapsedChapters.contains(chapter)) {
                out.add(new SidebarEntry(false, chapter, i));
            }
        }
        return out;
    }

    @Override
    protected void init() {
        if (pages.isEmpty()) {
            pages = WikiPageLoader.loadPages(namespace, basePath);
        }

        if (savedSidebarW > 0) {
            sidebarW = savedSidebarW;
        } else if (!pages.isEmpty()) {
            int widest = 0;
            for (WikiPageLoader.Page p : pages) widest = Math.max(widest, font.width(p.title()));
            sidebarW = widest + 20;
        }
        sidebarW = clampSidebarW(sidebarW);

        clearWidgets();

        searchBox = new EditBox(font, 4, HEADER_H + 3, sidebarW - 8, SEARCH_H, Component.empty());
        searchBox.setMaxLength(64);
        searchBox.setHint(Component.literal("§8Search…"));
        searchBox.setBordered(true);
        addRenderableWidget(searchBox);
        recomputeVisibleIndices();

        addRenderableWidget(Button.builder(Component.literal("§7✕ Close"),
                b -> {
                    if (minecraft != null) minecraft.setScreen(parent);
                })
                .bounds(width / 2 - 36, height - FOOTER_H + 6, 72, 16).build());

        addRenderableWidget(Button.builder(Component.literal("§7+ New"), b -> openNewPageEditor())
                .bounds(width - 8 - 44 - 4 - 44, 6, 44, 16).build());
        addRenderableWidget(Button.builder(Component.literal("§7✎ Edit"), b -> openEditCurrentPage())
                .bounds(width - 8 - 44, 6, 44, 16).build());
    }

    private void openEditCurrentPage() {
        if (pages.isEmpty() || minecraft == null) return;
        WikiPageLoader.Page page = pages.get(Math.min(activePage, pages.size() - 1));
        minecraft.setScreen(new WikiTextInputScreen(this, "Edit: " + page.title(), page.markdown(), 65536,
                v -> saveExistingPage(page, v)));
    }

    private void openNewPageEditor() {
        if (minecraft == null) return;
        minecraft.setScreen(new WikiTextInputScreen(this, "New Wiki Page (start with '# Title')", "# New Page\n\n",
                65536, this::createNewPage));
    }

    private void saveExistingPage(WikiPageLoader.Page page, String newMarkdown) {
        try {
            WikiPageLoader.savePage(namespace, basePath, WikiPageLoader.currentLocale(), page.filename(),
                    newMarkdown);
        } catch (java.io.IOException e) {
            LOGGER.error("[Phoenix Wiki] Failed to save wiki page '{}': {}", page.filename(), e.getMessage());
        }
        reloadPages(page.id());
    }

    private void createNewPage(String markdown) {
        String title = WikiPageLoader.deriveTitle(markdown, "New Page");
        String baseSlug = WikiPageLoader.slugify(title);
        Set<String> existingIds = new HashSet<>();
        for (WikiPageLoader.Page p : pages) existingIds.add(p.id());
        String id = baseSlug;
        int n = 1;
        while (existingIds.contains(id)) id = baseSlug + "_" + (++n);
        String filename = WikiPageLoader.nextFilename(pages, id);
        try {
            WikiPageLoader.savePage(namespace, basePath, WikiPageLoader.currentLocale(), filename, markdown);
        } catch (java.io.IOException e) {
            LOGGER.error("[Phoenix Wiki] Failed to create wiki page '{}': {}", filename, e.getMessage());
        }
        reloadPages(id);
    }

    private void reloadPages(String preferredActiveId) {
        pages = WikiPageLoader.loadPages(namespace, basePath);
        parsedCache.clear();
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).id().equals(preferredActiveId)) {
                activePage = i;
                break;
            }
        }
        scrollY = 0;
        recomputeVisibleIndices();
    }

    private int clampSidebarW(int w) {
        int upper = Math.min(MAX_SIDEBAR_W, width / 2);
        return Math.max(MIN_SIDEBAR_W, Math.min(upper, w));
    }

    private String lastSearchQuery = "";

    private void recomputeVisibleIndices() {
        String q = searchBox != null ? searchBox.getValue().trim().toLowerCase() : "";
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            if (q.isEmpty() || matchesSearch(pages.get(i), q)) out.add(i);
        }
        visibleIndices = out;

        if (!q.equals(lastSearchQuery)) {
            lastSearchQuery = q;
            if (!q.isEmpty() && !out.isEmpty()) {
                if (!out.contains(activePage)) activePage = out.get(0);
                jumpToSearchMatch(q);
            }
        }
    }

    private boolean matchesSearch(WikiPageLoader.Page page, String q) {
        return page.title().toLowerCase().contains(q) || page.markdown().toLowerCase().contains(q);
    }

    private void jumpToSearchMatch(String q) {
        if (pages.isEmpty()) return;
        List<RichBlock> blocks = activeBlocks();
        int cw = width - sidebarW - MARGIN * 2;
        if (cw <= 0) return;
        int y = 0;
        for (int i = 0; i < blocks.size(); i++) {
            if (blockContainsText(blocks.get(i), q)) {
                scrollY = Math.max(0, y - 10);
                return;
            }
            y = WikiRichTextRenderer.measureBlocksHeight(font, blocks.subList(i, i + 1), cw, expandedKeys) + y;
        }
        scrollY = 0;
    }

    private boolean blockContainsText(RichBlock b, String lowerQ) {
        if (b instanceof RichBlock.CodeBlock cb) return cb.code().toLowerCase().contains(lowerQ);
        if (b instanceof RichBlock.Callout c) {
            for (RichBlock child : c.children()) if (blockContainsText(child, lowerQ)) return true;
            return c.title() != null && c.title().toLowerCase().contains(lowerQ);
        }
        if (b instanceof RichBlock.Details d) {
            for (RichBlock child : d.children()) if (blockContainsText(child, lowerQ)) return true;
            return d.title() != null && d.title().toLowerCase().contains(lowerQ);
        }
        if (b instanceof RichBlock.Table t) {
            for (List<RichSpan> row : t.header())
                for (RichSpan s : row) if (spanText(s).toLowerCase().contains(lowerQ)) return true;
            for (List<List<RichSpan>> row : t.rows())
                for (List<RichSpan> cell : row)
                    for (RichSpan s : cell) if (spanText(s).toLowerCase().contains(lowerQ)) return true;
            return false;
        }
        for (RichSpan s : b.spans()) if (spanText(s).toLowerCase().contains(lowerQ)) return true;
        return false;
    }

    private String spanText(RichSpan s) {
        if (s instanceof RichSpan.Text t) return t.text();
        if (s instanceof RichSpan.Link l) return l.label();
        if (s instanceof RichSpan.Tip t) return t.label();
        return "";
    }

    private List<RichBlock> activeBlocks() {
        if (pages.isEmpty()) return List.of();
        WikiPageLoader.Page page = pages.get(Math.min(activePage, pages.size() - 1));
        return parsedCache.computeIfAbsent(page.id(), id -> {
            List<RichBlock> blocks = WikiMarkdownParser.parse(resolveContent(page));
            seedChecklistState(id, blocks);
            return blocks;
        });
    }

    private void seedChecklistState(String pageId, List<RichBlock> blocks) {
        ensureChecklistTrackerInit();
        for (RichBlock b : blocks) {
            if (b instanceof RichBlock.Checklist cl) {
                Boolean override = WikiChecklistProgress.getOverride(namespace, pageId, cl.checkKey());
                if (override != null) {
                    expandedKeys.add((override ? "CL1:" : "CL0:") + cl.checkKey());
                }
            }
        }
    }

    private void ensureChecklistTrackerInit() {
        if (WikiChecklistProgress.isInitialized()) return;
        if (minecraft == null) return;
        WikiChecklistProgress.init(minecraft.gameDirectory.toPath());
    }

    private void jumpToPage(String pageId) {
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).id().equals(pageId)) {
                activePage = i;
                scrollY = 0;
                return;
            }
        }
    }

    private String resolveContent(WikiPageLoader.Page page) {
        UnaryOperator<String> resolver = dynamicPageResolvers().get(page.id());
        return resolver != null ? resolver.apply(page.markdown()) : page.markdown();
    }

    protected Map<String, UnaryOperator<String>> dynamicPageResolvers() {
        return Map.of();
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics g) {}

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float partial) {
        recomputeVisibleIndices();

        g.fill(0, 0, width, height, theme.bg());

        String title = pages.isEmpty() ? "Wiki" : pages.get(Math.min(activePage, pages.size() - 1)).title();
        drawHeader(g, "§fWiki  §8— §7" + title);

        searchBox.setX(4);
        searchBox.setY(HEADER_H + 3);
        searchBox.setWidth(sidebarW - 8);

        int sidebarClipBot = height - FOOTER_H;
        int listTop = HEADER_H + LIST_TOP_OFFSET;
        g.enableScissor(0, listTop, sidebarW, sidebarClipBot);
        g.fill(0, listTop, sidebarW, sidebarClipBot, theme.panel());
        List<SidebarEntry> entries = buildSidebarEntries();
        for (int row = 0; row < entries.size(); row++) {
            SidebarEntry e = entries.get(row);
            int rowY = listTop + 4 + row * 16;
            if (rowY + 14 > sidebarClipBot) break;
            boolean hov = mx >= 0 && mx < sidebarW && my >= rowY - 1 && my < rowY + 15;
            if (e.isHeader()) {
                boolean collapsed = collapsedChapters.contains(e.chapter());
                if (hov) g.fill(0, rowY - 1, sidebarW, rowY + 15, 0x14FFFFFF);
                String label = (collapsed ? "§8▶ " : "§8▼ ") + WikiPageLoader.prettifyChapter(e.chapter());
                int maxW = sidebarW - 10;
                if (font.width(label.replaceAll("§.", "")) > maxW)
                    label = font.plainSubstrByWidth(label, maxW - 6) + "…";
                g.drawString(font, label, 4, rowY + 3, hov ? theme.textDim() : theme.textFaint(), false);
                continue;
            }
            int i = e.pageIndex();
            boolean sel = i == activePage;
            int indent = e.chapter() != null ? 14 : 8;
            if (sel) {
                g.fill(0, rowY - 1, sidebarW, rowY + 15, 0x22FFFFFF);
                g.fill(0, rowY - 1, 2, rowY + 15, theme.accent());
            } else if (hov) {
                g.fill(0, rowY - 1, sidebarW, rowY + 15, 0x14FFFFFF);
            }
            int textCol = sel ? theme.text() : (hov ? theme.text() : theme.textDim());
            String pageTitle = pages.get(i).title();
            int maxTitleW = sidebarW - indent - 4;
            if (font.width(pageTitle) > maxTitleW) pageTitle = font.plainSubstrByWidth(pageTitle, maxTitleW - 6) + "…";
            g.drawString(font, pageTitle, indent, rowY + 3, textCol, false);
        }
        if (entries.isEmpty()) {
            g.drawString(font, "§8No matches", 8, listTop + 6, theme.textFaint(), false);
        }
        g.disableScissor();

        boolean handleHov = mx >= sidebarW - HANDLE_W / 2 && mx < sidebarW + HANDLE_W / 2 + 1 &&
                my >= HEADER_H && my < height - FOOTER_H;
        g.fill(sidebarW - 1, HEADER_H, sidebarW, height - FOOTER_H, theme.border());
        if (handleHov || draggingSidebar) {
            g.fill(sidebarW - 1, HEADER_H, sidebarW + 1, height - FOOTER_H, theme.accent());
        }

        drawFooter(g);

        int cx = sidebarW + MARGIN;
        int cw = width - cx - MARGIN;
        int contentTop = HEADER_H + MARGIN;
        int contentBot = height - FOOTER_H - MARGIN;

        g.enableScissor(cx, contentTop, cx + cw, contentBot);
        List<RichBlock> blocks = activeBlocks();
        richRegions = WikiRichTextRenderer.renderBlocks(g, font, blocks, cx, contentTop, cw,
                scrollY, contentTop, contentBot, theme.accent(), expandedKeys);
        g.disableScissor();

        cachedContentH = WikiRichTextRenderer.measureBlocksHeight(font, blocks, cw, expandedKeys);
        drawScrollbar(g, width - MARGIN / 2, contentTop, contentBot, scrollY, cachedContentH);

        renderToc(g, blocks, cx, cw, contentTop, contentBot, mx, my);

        for (RichSpan.Region r : richRegions) {
            if (!r.contains(mx, my)) continue;
            if (r.span() instanceof RichSpan.Tip t) {
                g.pose().pushPose();
                g.pose().translate(0f, 0f, 500f);
                List<net.minecraft.util.FormattedCharSequence> lines =
                        font.split(Component.literal(t.tooltip()), 240);
                g.renderTooltip(font, lines, mx, my);
                g.pose().popPose();
                break;
            }
            if (r.span() instanceof RichSpan.ItemIcon icon && icon.tooltip() != null && !icon.tooltip().isBlank()) {
                g.pose().pushPose();
                g.pose().translate(0f, 0f, 500f);
                List<net.minecraft.util.FormattedCharSequence> lines =
                        font.split(Component.literal(icon.tooltip()), 240);
                g.renderTooltip(font, lines, mx, my);
                g.pose().popPose();
                break;
            }
        }

        if (justCopiedCode != null) {
            if (System.currentTimeMillis() - justCopiedAtMs > COPY_FLASH_MS) {
                justCopiedCode = null;
            } else {
                for (RichSpan.Region r : richRegions) {
                    if (r.span() instanceof RichSpan.CodeCopy cc && cc.code().equals(justCopiedCode)) {
                        String msg = "§a✓ Copied!";
                        int labelX = cx + cw - font.width(msg) - 4;
                        g.pose().pushPose();
                        g.pose().translate(0f, 0f, 400f);
                        g.drawString(font, msg, labelX, r.y1() + 1, 0xFFFFFFFF, false);
                        g.pose().popPose();
                        break;
                    }
                }
            }
        }

        super.render(g, mx, my, partial);
    }

    private void drawHeader(GuiGraphics g, String titleText) {
        g.fill(0, 0, width, HEADER_H, theme.header());
        g.fill(0, HEADER_H - 1, width, HEADER_H, theme.border());
        g.drawCenteredString(font, titleText, width / 2, (HEADER_H / 2) - (font.lineHeight / 2), theme.text());
    }

    private void drawFooter(GuiGraphics g) {
        int topY = height - FOOTER_H;
        g.fill(0, topY, width, height, theme.header());
        g.fill(0, topY, width, topY + 1, theme.border());
    }

    private static void drawScrollbar(GuiGraphics g, int rightMarginX, int topBoundsY, int bottomBoundsY,
                                      int currentScrollY, int totalContentHeight) {
        int visibleHeight = bottomBoundsY - topBoundsY;
        if (totalContentHeight <= visibleHeight) return;

        int trackWidth = 2;
        int scrollTrackLeft = rightMarginX - trackWidth;

        int thumbHeight = Math.max(16, (visibleHeight * visibleHeight) / totalContentHeight);
        long maxScrollableDistance = totalContentHeight - visibleHeight;
        if (maxScrollableDistance <= 0) return;

        int thumbY = topBoundsY + (int) ((long) currentScrollY * (visibleHeight - thumbHeight) / maxScrollableDistance);

        g.fill(scrollTrackLeft, topBoundsY, rightMarginX, bottomBoundsY, 0x22FFFFFF);
        g.fill(scrollTrackLeft, thumbY, rightMarginX, thumbY + thumbHeight, 0x88FFFFFF);
    }

    private void renderToc(GuiGraphics g, List<RichBlock> blocks, int cx, int cw, int contentTop, int contentBot,
                           int mx, int my) {
        List<WikiRichTextRenderer.HeadingInfo> headings =
                WikiRichTextRenderer.computeHeadingOffsets(font, blocks, cw);
        List<RichSpan.Region> regions = new ArrayList<>();
        if (headings.size() < 2) {
            tocRegions = regions;
            return;
        }

        int boxX = cx + cw - TOC_W;
        int boxY = contentTop;
        String toggleLabel = tocOpen ? "§7On this page ▾" : "§7On this page ▸";
        int toggleH = 12;
        boolean toggleHov = mx >= boxX && mx < boxX + TOC_W && my >= boxY && my < boxY + toggleH;

        g.pose().pushPose();
        g.pose().translate(0f, 0f, 300f);
        g.fill(boxX - 4, boxY - 2, boxX + TOC_W, boxY + toggleH + 1, 0xDD16121C);
        g.drawString(font, toggleHov ? "§f" + toggleLabel.substring(2) : toggleLabel, boxX, boxY, 0xFFCFCFDA, false);
        regions.add(new RichSpan.Region(boxX - 4, boxY - 2, boxX + TOC_W, boxY + toggleH + 1,
                new RichSpan.TocJump(-1)));

        if (tocOpen) {
            int rowY = boxY + toggleH + 2;
            int maxScroll = Math.max(0, cachedContentH - (contentBot - contentTop));
            for (WikiRichTextRenderer.HeadingInfo hInfo : headings) {
                String label = hInfo.text();
                int indent = (hInfo.level() - 1) * 6;
                int maxLabelW = TOC_W - indent - 4;
                if (font.width(label) > maxLabelW) label = font.plainSubstrByWidth(label, maxLabelW - 6) + "…";
                boolean hov = mx >= boxX && mx < boxX + TOC_W && my >= rowY - 1 && my < rowY + 9;
                g.fill(boxX - 4, rowY - 1, boxX + TOC_W, rowY + 9, hov ? 0xDD22182E : 0xDD16121C);
                g.drawString(font, label, boxX + indent, rowY, hov ? 0xFFFFFFFF : 0xFFAFA8BE, false);
                int target = Math.max(0, Math.min(maxScroll, hInfo.y()));
                regions.add(new RichSpan.Region(boxX - 4, rowY - 1, boxX + TOC_W, rowY + 9,
                        new RichSpan.TocJump(target)));
                rowY += 10;
            }
        }
        g.pose().popPose();
        tocRegions = regions;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        int visibleH = (height - FOOTER_H - MARGIN) - (HEADER_H + MARGIN);
        int maxScroll = Math.max(0, cachedContentH - visibleH);
        scrollY = Math.max(0, Math.min(maxScroll, (int) (scrollY - delta * 14)));
        return true;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0) {
            for (RichSpan.Region r : tocRegions) {
                if (r.contains(mx, my) && r.span() instanceof RichSpan.TocJump jump) {
                    if (jump.targetY() < 0) {
                        tocOpen = !tocOpen;
                    } else {
                        scrollY = jump.targetY();
                    }
                    return true;
                }
            }

            if (mx >= sidebarW - HANDLE_W / 2 && mx < sidebarW + HANDLE_W / 2 + 1 &&
                    my >= HEADER_H && my < height - FOOTER_H) {
                draggingSidebar = true;
                return true;
            }

            int sidebarClipBot = height - FOOTER_H;
            int listTop = HEADER_H + LIST_TOP_OFFSET;
            if (mx >= 0 && mx < sidebarW && my >= listTop) {
                List<SidebarEntry> entries = buildSidebarEntries();
                for (int row = 0; row < entries.size(); row++) {
                    int rowY = listTop + 4 + row * 16;
                    if (rowY + 14 > sidebarClipBot) break;
                    if (my >= rowY - 1 && my < rowY + 15) {
                        SidebarEntry e = entries.get(row);
                        if (e.isHeader()) {
                            if (!collapsedChapters.remove(e.chapter())) collapsedChapters.add(e.chapter());
                        } else {
                            activePage = e.pageIndex();
                            scrollY = 0;
                        }
                        return true;
                    }
                }
            }

            for (RichSpan.Region r : richRegions) {
                if (!r.contains(mx, my)) continue;
                if (r.span() instanceof RichSpan.Link l) {
                    if (l.url().startsWith("wiki:")) {
                        jumpToPage(l.url().substring(5));
                    } else {
                        try {
                            java.awt.Desktop.getDesktop().browse(java.net.URI.create(l.url()));
                        } catch (Exception ignored) {}
                    }
                    return true;
                }
                if (r.span() instanceof RichSpan.CodeCopy cc) {
                    if (minecraft != null) minecraft.keyboardHandler.setClipboard(cc.code());
                    justCopiedCode = cc.code();
                    justCopiedAtMs = System.currentTimeMillis();
                    return true;
                }
                if (r.span() instanceof RichSpan.DetailsToggle dt) {
                    if (!expandedKeys.remove(dt.key())) expandedKeys.add(dt.key());
                    return true;
                }
                if (r.span() instanceof RichSpan.ChecklistToggle ct) {
                    boolean current = expandedKeys.contains("CL1:" + ct.key())
                            || (!expandedKeys.contains("CL0:" + ct.key()) && ct.checkedDefault());
                    boolean next = !current;
                    expandedKeys.remove("CL1:" + ct.key());
                    expandedKeys.remove("CL0:" + ct.key());
                    expandedKeys.add((next ? "CL1:" : "CL0:") + ct.key());
                    if (!pages.isEmpty()) {
                        WikiPageLoader.Page page = pages.get(Math.min(activePage, pages.size() - 1));
                        WikiChecklistProgress.setChecked(namespace, page.id(), ct.key(), next);
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dragX, double dragY) {
        if (draggingSidebar) {
            sidebarW = clampSidebarW((int) mx);
            return true;
        }
        return super.mouseDragged(mx, my, btn, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        if (draggingSidebar) {
            draggingSidebar = false;
            savedSidebarW = sidebarW;
            return true;
        }
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == 256) {
            if (searchBox != null && searchBox.isFocused() && !searchBox.getValue().isEmpty()) {
                searchBox.setValue("");
                return true;
            }
            if (minecraft != null) minecraft.setScreen(parent);
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
