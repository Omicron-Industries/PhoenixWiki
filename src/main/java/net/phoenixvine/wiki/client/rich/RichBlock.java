package net.phoenixvine.wiki.client.rich;

import java.util.List;

public sealed interface RichBlock {

    List<RichSpan> spans();

    record Heading(int level, List<RichSpan> spans) implements RichBlock {}

    record Paragraph(List<RichSpan> spans) implements RichBlock {}

    record ListItem(String marker, int indent, List<RichSpan> spans) implements RichBlock {}

    /**
     * A "- [ ] text" / "- [x] text" checklist item. checkKey is unique within the parsed document
     * only (not across pages) - callers persisting checked state must scope it by page id.
     * checkedDefault reflects the markdown source's own [x]/[ ] state, used until the player
     * explicitly toggles it.
     */
    record Checklist(String checkKey, boolean checkedDefault, int indent, List<RichSpan> spans) implements RichBlock {}

    record Quote(List<RichSpan> spans) implements RichBlock {}

    record CodeBlock(String lang, String code) implements RichBlock {

        @Override
        public List<RichSpan> spans() {
            return List.of();
        }
    }

    /** type: "note" | "warning" | "tip" | "info" - from a ::: type Title ... ::: container. */
    record Callout(String type, String title, List<RichBlock> children) implements RichBlock {

        @Override
        public List<RichSpan> spans() {
            return List.of();
        }
    }

    /** A collapsible ::: spoiler Title ... ::: container. expandKey is unique per page+title for toggle state. */
    record Details(String expandKey, String title, List<RichBlock> children) implements RichBlock {

        @Override
        public List<RichSpan> spans() {
            return List.of();
        }
    }

    record Table(List<List<RichSpan>> header, List<List<List<RichSpan>>> rows) implements RichBlock {

        @Override
        public List<RichSpan> spans() {
            return List.of();
        }
    }

    record Rule() implements RichBlock {

        @Override
        public List<RichSpan> spans() {
            return List.of();
        }
    }

    record Blank() implements RichBlock {

        @Override
        public List<RichSpan> spans() {
            return List.of();
        }
    }
}
