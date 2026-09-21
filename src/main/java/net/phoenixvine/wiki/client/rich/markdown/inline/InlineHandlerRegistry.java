package net.phoenixvine.wiki.client.rich.markdown.inline;

import net.phoenixvine.wiki.client.rich.markdown.inline.handlers.BoldItalicHandler;
import net.phoenixvine.wiki.client.rich.markdown.inline.handlers.BracketDirectiveHandler;
import net.phoenixvine.wiki.client.rich.markdown.inline.handlers.ColorTokenHandler;
import net.phoenixvine.wiki.client.rich.markdown.inline.handlers.FootnoteRefHandler;
import net.phoenixvine.wiki.client.rich.markdown.inline.handlers.HighlightHandler;
import net.phoenixvine.wiki.client.rich.markdown.inline.handlers.InlineCodeHandler;
import net.phoenixvine.wiki.client.rich.markdown.inline.handlers.KbdHandler;
import net.phoenixvine.wiki.client.rich.markdown.inline.handlers.LinkTargetHandler;
import net.phoenixvine.wiki.client.rich.markdown.inline.handlers.StrikethroughHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InlineHandlerRegistry {

    public static final InlineHandlerRegistry DEFAULT = new InlineHandlerRegistry()
            .register(new ColorTokenHandler())
            .register(new FootnoteRefHandler())
            .register(new LinkTargetHandler())
            .register(new BracketDirectiveHandler())
            .register(new InlineCodeHandler())
            .register(new KbdHandler())
            .register(new StrikethroughHandler())
            .register(new HighlightHandler())
            .register(new BoldItalicHandler());

    private final Map<Character, List<InlineHandler>> byTrigger = new HashMap<>();

    public InlineHandlerRegistry register(InlineHandler handler) {
        byTrigger.computeIfAbsent(handler.trigger(), k -> new ArrayList<>()).add(handler);
        return this;
    }

    List<InlineHandler> forTrigger(char c) {
        return byTrigger.getOrDefault(c, List.of());
    }
}
