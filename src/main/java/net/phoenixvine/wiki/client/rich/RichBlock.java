package net.phoenixvine.wiki.client.rich;

import java.util.List;

public sealed interface RichBlock {

    List<RichSpan> spans();

    record Heading(int level, List<RichSpan> spans) implements RichBlock {}

    record Paragraph(List<RichSpan> spans) implements RichBlock {}

    record ListItem(String marker, int indent, List<RichSpan> spans) implements RichBlock {}

    record Checklist(String checkKey, boolean checkedDefault, int indent, List<RichSpan> spans) implements RichBlock {}

    record Quote(List<RichSpan> spans) implements RichBlock {}

    record CodeBlock(String lang, String code) implements RichBlock {

        @Override
        public List<RichSpan> spans() {
            return List.of();
        }
    }

    record Callout(String type, String title, List<RichBlock> children) implements RichBlock {

        @Override
        public List<RichSpan> spans() {
            return List.of();
        }
    }

    record Details(String expandKey, String title, List<RichBlock> children) implements RichBlock {

        @Override
        public List<RichSpan> spans() {
            return List.of();
        }
    }

    /**
     * A heading (H1-H3) plus everything under it up to the next heading of the same-or-shallower
     * level, collapsible by clicking the heading itself - unlike {@link Details}, the heading keeps
     * its normal heading size/color/weight rather than being replaced with a "Details" bullet
     * header. Defaults to expanded; {@code collapseKey} is checked by the renderer with inverted
     * semantics from Details' expandKey (present in the tracked key set = collapsed, not expanded).
     */
    record CollapsibleSection(int level, List<RichSpan> headingSpans, String collapseKey,
                              List<RichBlock> children) implements RichBlock {

        @Override
        public List<RichSpan> spans() {
            return headingSpans;
        }
    }

    /**
     * A page-level {@code {scale:1.2}} directive (its own line, anywhere in the source, usually the
     * first) - invisible in the rendered page, just tells the host screen to multiply its base text
     * scale for this one page. See WikiMarkdownParser's SCALE_DIRECTIVE pattern.
     */
    record ScaleDirective(float multiplier) implements RichBlock {

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
