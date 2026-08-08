package net.phoenixvine.wiki.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WikiPageLoader {

    private WikiPageLoader() {}

    private static final String DEFAULT_LOCALE = "en_us";

    public record Page(String id, String title, String markdown, String filename, String chapter) {

        public String chapterLabel() {
            return prettifyChapter(chapter);
        }
    }

    public static String prettifyChapter(String chapter) {
        if (chapter == null) return null;
        StringBuilder sb = new StringBuilder();
        for (String part : chapter.split("[_-]")) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    public static List<Page> loadPages(String namespace, String basePath) {
        String locale = currentLocale();
        List<Page> assetPages = loadFromAssets(namespace, basePath, locale);
        if (assetPages.isEmpty() && !locale.equals(DEFAULT_LOCALE)) {
            assetPages = loadFromAssets(namespace, basePath, DEFAULT_LOCALE);
        }
        List<Page> configPages = loadFromConfig(namespace, basePath, locale);

        Map<String, Page> byId = new LinkedHashMap<>();
        for (Page p : assetPages) byId.put(p.id(), p);
        for (Page p : configPages) byId.put(p.id(), p);
        return new ArrayList<>(byId.values());
    }

    public static Path configDir(String namespace, String basePath, String locale) {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config").resolve(namespace).resolve(basePath).resolve(locale);
    }

    public static void savePage(String namespace, String basePath, String locale, String filename, String markdown)
                                                                                                                    throws IOException {
        Path dir = configDir(namespace, basePath, locale);
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(filename), markdown, StandardCharsets.UTF_8);
    }

    public static String nextFilename(List<Page> pages, String slug) {
        int max = 0;
        for (Page p : pages) {
            String fn = p.filename();
            int underscore = fn.indexOf('_');
            if (underscore > 0) {
                try {
                    max = Math.max(max, Integer.parseInt(fn.substring(0, underscore)));
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("%02d_%s.md", max + 1, slug);
    }

    public static String slugify(String title) {
        String slug = title.toLowerCase().trim().replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        return slug.isEmpty() ? "page" : slug;
    }

    public static String currentLocale() {
        try {
            String code = Minecraft.getInstance().getLanguageManager().getSelected();
            return code != null && !code.isBlank() ? code : DEFAULT_LOCALE;
        } catch (Exception e) {
            return DEFAULT_LOCALE;
        }
    }

    private static List<Page> loadFromAssets(String namespace, String basePath, String locale) {
        Map<ResourceLocation, Resource> found;
        try {
            found = Minecraft.getInstance().getResourceManager().listResources(basePath + "/" + locale,
                    rl -> rl.getNamespace().equals(namespace) && rl.getPath().endsWith(".md"));
        } catch (Exception e) {
            return List.of();
        }

        List<Map.Entry<ResourceLocation, Resource>> sorted = new ArrayList<>(found.entrySet());
        sorted.sort(Comparator.comparing(e -> e.getKey().getPath()));

        String prefix = basePath + "/" + locale + "/";
        List<Page> pages = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Resource> entry : sorted) {
            try (InputStream in = entry.getValue().open()) {
                String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                String path = entry.getKey().getPath();
                String rel = path.startsWith(prefix) ? path.substring(prefix.length()) :
                        path.substring(path.lastIndexOf('/') + 1);
                String chapter = null;
                if (rel.contains("/")) {
                    chapter = rel.substring(0, rel.indexOf('/'));
                    rel = rel.substring(rel.indexOf('/') + 1);
                }
                String filename = rel;
                String id = filename.substring(0, filename.length() - 3).replaceFirst("^\\d+_", "");
                pages.add(new Page(id, deriveTitle(content, id), content, filename, chapter));
            } catch (IOException ignored) {}
        }
        return pages;
    }

    private static List<Page> loadFromConfig(String namespace, String basePath, String locale) {
        Path dir = configDir(namespace, basePath, locale);
        if (!Files.isDirectory(dir)) return List.of();

        List<Path> files;
        try (var stream = Files.walk(dir, 2)) {
            files = stream.filter(p -> !Files.isDirectory(p) && p.getFileName().toString().endsWith(".md"))
                    .sorted(Comparator.comparing(p -> dir.relativize(p).toString()))
                    .toList();
        } catch (IOException e) {
            return List.of();
        }

        List<Page> pages = new ArrayList<>();
        for (Path file : files) {
            try {
                String content = Files.readString(file, StandardCharsets.UTF_8);
                String filename = file.getFileName().toString();
                Path relParent = dir.relativize(file).getParent();
                String chapter = relParent != null ? relParent.toString() : null;
                String id = filename.substring(0, filename.length() - 3).replaceFirst("^\\d+_", "");
                pages.add(new Page(id, deriveTitle(content, id), content, filename, chapter));
            } catch (IOException ignored) {}
        }
        return pages;
    }

    public static String deriveTitle(String markdown, String fallbackId) {
        for (String line : markdown.split("\n", -1)) {
            String t = line.trim();
            if (t.startsWith("# ")) return t.substring(2).trim();
        }
        StringBuilder sb = new StringBuilder();
        for (String part : fallbackId.split("[_-]")) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }
}
