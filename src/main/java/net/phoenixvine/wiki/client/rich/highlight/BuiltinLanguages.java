package net.phoenixvine.wiki.client.rich.highlight;

import java.util.Set;

final class BuiltinLanguages {

    private BuiltinLanguages() {}

    static void registerAll() {
        LanguageRegistry.register(new LanguageDefinition(Set.of(
                "public", "private", "protected", "class", "interface", "extends", "implements", "static", "final",
                "void", "new", "return", "if", "else", "for", "while", "do", "switch", "case", "break", "continue",
                "try", "catch", "finally", "throw", "throws", "import", "package", "this", "super", "true", "false",
                "null", "default", "enum", "record", "sealed", "permits", "yield", "abstract", "instanceof",
                "int", "long", "double", "float", "boolean", "char", "byte", "short", "synchronized", "volatile",
                "transient", "native"
        ), false, true), "java");

        LanguageDefinition js = new LanguageDefinition(Set.of(
                "function", "const", "let", "var", "typeof", "instanceof", "in", "of", "new", "return", "if", "else",
                "for", "while", "do", "switch", "case", "break", "continue", "try", "catch", "finally", "throw",
                "import", "export", "from", "default", "class", "extends", "super", "this", "true", "false", "null",
                "undefined", "async", "await", "yield", "static", "get", "set", "delete", "void"
        ), true, false);
        LanguageRegistry.register(js, "js", "javascript");

        LanguageDefinition ts = new LanguageDefinition(Set.of(
                "function", "const", "let", "var", "typeof", "instanceof", "in", "of", "new", "return", "if", "else",
                "for", "while", "do", "switch", "case", "break", "continue", "try", "catch", "finally", "throw",
                "import", "export", "from", "default", "class", "extends", "implements", "interface", "type",
                "super", "this", "true", "false", "null", "undefined", "async", "await", "yield", "static",
                "public", "private", "protected", "readonly", "enum", "namespace", "declare", "as"
        ), true, false);
        LanguageRegistry.register(ts, "ts", "typescript");

        LanguageRegistry.register(new LanguageDefinition(Set.of(
                "fun", "val", "var", "class", "interface", "object", "companion", "override", "private", "public",
                "protected", "internal", "return", "if", "else", "for", "while", "do", "when", "is", "in", "true",
                "false", "null", "this", "super", "import", "package", "data", "sealed", "abstract", "open", "final",
                "suspend", "inline", "reified", "vararg", "lateinit"
        ), false, false), "kotlin");

        LanguageRegistry.register(new LanguageDefinition(Set.of("true", "false", "null"), false, false), "json");

        LanguageRegistry.register(new LanguageDefinition(Set.of(
                "auto", "break", "case", "char", "const", "continue", "default", "do", "double", "else", "enum",
                "extern", "float", "for", "goto", "if", "int", "long", "register", "return", "short", "signed",
                "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while"
        ), true, true), "c");

        LanguageDefinition csharp = new LanguageDefinition(Set.of(
                "abstract", "as", "base", "bool", "break", "byte", "case", "catch", "char", "checked", "class",
                "const", "continue", "decimal", "default", "delegate", "do", "double", "else", "enum", "event",
                "explicit", "extern", "false", "finally", "fixed", "float", "for", "foreach", "goto", "if",
                "implicit", "in", "int", "interface", "internal", "is", "lock", "long", "namespace", "new",
                "null", "object", "operator", "out", "override", "params", "private", "protected", "public",
                "readonly", "ref", "return", "sbyte", "sealed", "short", "sizeof", "stackalloc", "static",
                "string", "struct", "switch", "this", "throw", "true", "try", "typeof", "uint", "ulong",
                "unchecked", "unsafe", "ushort", "using", "virtual", "void", "volatile", "while", "async",
                "await", "var"
        ), true, true);
        LanguageRegistry.register(csharp, "cs", "csharp");

        LanguageDefinition rust = new LanguageDefinition(Set.of(
                "as", "async", "await", "break", "const", "continue", "crate", "dyn", "else", "enum", "extern",
                "false", "fn", "for", "if", "impl", "in", "let", "loop", "match", "mod", "move", "mut", "pub",
                "ref", "return", "self", "Self", "static", "struct", "super", "trait", "true", "type", "unsafe",
                "use", "where", "while"
        ), true, true);
        LanguageRegistry.register(rust, "rs", "rust");

        LanguageRegistry.register(new LanguageDefinition(Set.of(
                "False", "None", "True", "and", "as", "assert", "async", "await", "break", "class", "continue",
                "def", "del", "elif", "else", "except", "finally", "for", "from", "global", "if", "import",
                "in", "is", "lambda", "nonlocal", "not", "or", "pass", "raise", "return", "try", "while",
                "with", "yield"
        ), true, false), "py", "python");

        LanguageRegistry.register(new LanguageDefinition(Set.of(
                "fn", "struct", "let", "var", "if", "else", "while", "for", "in", "return", "true", "false",
                "impl", "mut", "arena", "component", "resource", "system", "interface", "dyn", "enum", "match",
                "by", "sealed", "extern", "class", "module", "pub", "open", "extend", "use", "static", "as",
                "try", "catch", "throw", "extends", "override", "null", "is", "break", "continue", "dev", "parallel"
        ), true, true), "hotchocolate", "hc", "hotc");
    }
}