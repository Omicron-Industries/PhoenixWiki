package net.phoenixvine.wiki.client.rich.render;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Backs the opt-in {@code :::loading} collapsible (see {@link net.phoenixvine.wiki.client.rich.RichBlock.Details#fakeLoading()}):
 * a brief, purely cosmetic delay between expanding a block and its real content appearing.
 * Static/global rather than threaded through RenderContext -- this is ephemeral UI timing, not
 * state any caller needs to read, so it isn't worth widening the shared renderer's signature for.
 */
public final class FakeLoadingTimers {

    private FakeLoadingTimers() {}

    public static final long DELAY_MS = 550L;

    private static final Map<String, Long> startedAtMs = new ConcurrentHashMap<>();

    public static void start(String expandKey) {
        startedAtMs.putIfAbsent(expandKey, System.currentTimeMillis());
    }

    public static boolean isLoading(String expandKey) {
        Long startedAt = startedAtMs.get(expandKey);
        if (startedAt == null) return false;
        if (System.currentTimeMillis() - startedAt >= DELAY_MS) {
            startedAtMs.remove(expandKey);
            return false;
        }
        return true;
    }
}
