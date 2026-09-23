package net.phoenixvine.wiki.client.rich.highlight;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class LanguageRegistry {

    private static final Map<String, LanguageDefinition> LANGUAGES = new ConcurrentHashMap<>();

    static {
        BuiltinLanguages.registerAll();
    }

    private LanguageRegistry() {}

    public static void register(LanguageDefinition definition, String... aliases) {
        for (String alias : aliases) {
            LANGUAGES.put(alias.toLowerCase(), definition);
        }
    }

    public static Optional<LanguageDefinition> forLang(String lang) {
        if (lang == null) return Optional.empty();
        return Optional.ofNullable(LANGUAGES.get(lang.toLowerCase()));
    }

    public static boolean isSupported(String lang) {
        return forLang(lang).isPresent();
    }
}
