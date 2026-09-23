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
        int offsetX = 0;
        int offsetY = 0;
    }

    private static Data data = null;

    public static void init() {
        synchronized (LOCK) {
            ensureLoaded();
        }
    }

    private static File file() {
        return new File(Minecraft.getInstance().gameDirectory, "config/phoenix_wiki/suite_hud.json");
    }

    private static File legacyFile() {
        return new File(Minecraft.getInstance().gameDirectory, "config/phoenix-wiki-suite.json");
    }

    private static void ensureLoaded() {
        if (data != null) return;
        data = new Data();
        var file = file();
        if (!file.exists() && legacyFile().exists()) {
            file = legacyFile();
        }
        if (file.exists()) {
            try (FileReader r = new FileReader(file)) {
                JsonElement root = JsonParser.parseReader(r);
                if (root != null && root.isJsonArray()) {
                    
                    for (JsonElement e : root.getAsJsonArray()) data.disabled.add(e.getAsString());
                } else if (root != null && root.isJsonObject()) {
                    Data loaded = GSON.fromJson(root, Data.class);
                    if (loaded != null) {
                        if (loaded.disabled != null) data.disabled.addAll(loaded.disabled);
                        if (loaded.buttonScale != null) data.buttonScale.putAll(loaded.buttonScale);
                        data.globalScale = clamp(loaded.globalScale <= 0f ? 1.0f : loaded.globalScale);
                        data.offsetX = loaded.offsetX;
                        data.offsetY = loaded.offsetY;
                    }
                }
            } catch (Exception e) {
                PhoenixWiki.LOGGER.warn("[PhoenixWiki] Failed to load suite config: {}", e.getMessage());
            }
        }

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

    public static int getOffsetX() {
        synchronized (LOCK) {
            ensureLoaded();
            return data.offsetX;
        }
    }

    public static int getOffsetY() {
        synchronized (LOCK) {
            ensureLoaded();
            return data.offsetY;
        }
    }

    /** Called every frame while a drag is in progress -- doesn't hit disk, just updates memory. */
    public static void setOffsetLive(int x, int y) {
        synchronized (LOCK) {
            ensureLoaded();
            data.offsetX = x;
            data.offsetY = y;
        }
    }

    /** Called once when a drag ends, to persist the final position. */
    public static void commitOffset() {
        synchronized (LOCK) {
            ensureLoaded();
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
