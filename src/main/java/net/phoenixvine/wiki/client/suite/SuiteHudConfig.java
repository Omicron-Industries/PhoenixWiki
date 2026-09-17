package net.phoenixvine.wiki.client.suite;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.phoenixvine.wiki.PhoenixWiki;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class SuiteHudConfig {

    private SuiteHudConfig() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Object LOCK = new Object();

    public static final float MIN_SCALE = 0.5f;
    public static final float MAX_SCALE = 3.0f;

    private static final class Data {

        Set<String> disabled = new HashSet<>();
        float globalScale = 1.0f;
        Map<String, Float> buttonScale = new HashMap<>();
    }

    private static Data data = null;

    /**
     * Forces the config to load (and, per {@link #ensureLoaded}, write itself back out) right now
     * instead of waiting for whichever mod's HUD button happens to render first -- nothing calls
     * isEnabled()/getEffectiveScale() etc. until a screen with the suite bar actually opens, so
     * without this the file (and its config/phoenix_wiki/ folder) might never appear at all if the
     * player never opens an inventory-adjacent screen. Called once from PhoenixWiki's own client setup.
     */
    public static void init() {
        synchronized (LOCK) {
            ensureLoaded();
        }
    }

    private static File file() {
        return new File(Minecraft.getInstance().gameDirectory, "config/phoenix_wiki/suite_hud.json");
    }

    /** Where this file lived before every PhoenixWiki config got its own subfolder -- see {@link #ensureLoaded}. */
    private static File legacyFile() {
        return new File(Minecraft.getInstance().gameDirectory, "config/phoenix-wiki-suite.json");
    }

    private static void ensureLoaded() {
        if (data != null) return;
        data = new Data();
        File f = file();
        if (!f.exists() && legacyFile().exists()) {
            f = legacyFile();
        }
        if (f.exists()) {
            try (FileReader r = new FileReader(f)) {
                JsonElement root = JsonParser.parseReader(r);
                if (root != null && root.isJsonArray()) {
                    
                    for (JsonElement e : root.getAsJsonArray()) data.disabled.add(e.getAsString());
                } else if (root != null && root.isJsonObject()) {
                    Data loaded = GSON.fromJson(root, Data.class);
                    if (loaded != null) {
                        if (loaded.disabled != null) data.disabled.addAll(loaded.disabled);
                        if (loaded.buttonScale != null) data.buttonScale.putAll(loaded.buttonScale);
                        data.globalScale = clamp(loaded.globalScale <= 0f ? 1.0f : loaded.globalScale);
                    }
                }
            } catch (Exception e) {
                PhoenixWiki.LOGGER.warn("[PhoenixWiki] Failed to load suite config: {}", e.getMessage());
            }
        }
        // Always write back, not just when migrating from the old path -- otherwise a totally fresh
        // install (no file either old or new) never creates config/phoenix_wiki/ at all until the
        // player happens to change a HUD setting, unlike every other mod's config showing up on first load.
        save();
    }

    private static float clamp(float scale) {
        return Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));
    }

    public static boolean isEnabled(String modId) {
        synchronized (LOCK) {
            ensureLoaded();
            return !data.disabled.contains(modId);
        }
    }

    public static void setEnabled(String modId, boolean enabled) {
        synchronized (LOCK) {
            ensureLoaded();
            if (enabled) data.disabled.remove(modId);
            else data.disabled.add(modId);
            save();
        }
    }

    public static void toggleEnabled(String modId) {
        setEnabled(modId, !isEnabled(modId));
    }

    public static float getGlobalScale() {
        synchronized (LOCK) {
            ensureLoaded();
            return data.globalScale;
        }
    }

    public static void setGlobalScale(float scale) {
        synchronized (LOCK) {
            ensureLoaded();
            data.globalScale = clamp(scale);
            save();
        }
    }

    public static float getEffectiveScale(String modId) {
        synchronized (LOCK) {
            ensureLoaded();
            Float override = data.buttonScale.get(modId);
            return override != null ? override : data.globalScale;
        }
    }

    public static Float getButtonScaleOverride(String modId) {
        synchronized (LOCK) {
            ensureLoaded();
            return data.buttonScale.get(modId);
        }
    }

    public static void setButtonScale(String modId, Float scale) {
        synchronized (LOCK) {
            ensureLoaded();
            if (scale == null) data.buttonScale.remove(modId);
            else data.buttonScale.put(modId, clamp(scale));
            save();
        }
    }

    private static void save() {
        try {
            File f = file();
            if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
            try (FileWriter w = new FileWriter(f)) {
                GSON.toJson(data, w);
            }
        } catch (Exception e) {
            PhoenixWiki.LOGGER.warn("[PhoenixWiki] Failed to save suite config: {}", e.getMessage());
        }
    }
}
