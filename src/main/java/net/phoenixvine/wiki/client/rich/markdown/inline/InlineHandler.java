package net.phoenixvine.wiki.client.rich.markdown.inline;


public interface InlineHandler {
    char trigger();

    int tryHandle(InlineParseState s, int i);
}
