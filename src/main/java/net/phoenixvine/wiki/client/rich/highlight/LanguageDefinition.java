package net.phoenixvine.wiki.client.rich.highlight;

import java.util.Set;

public record LanguageDefinition(Set<String> keywords, boolean templateLiterals, boolean annotations) {

    public LanguageDefinition {
        keywords = Set.copyOf(keywords);
    }
}
