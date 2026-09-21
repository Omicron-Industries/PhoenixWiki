package net.phoenixvine.wiki.client.rich;

import java.util.List;

/**
 * A single block-level element of a parsed wiki page.
 *
 * This is intentionally NOT sealed: any mod can add its own block type by implementing this
 * interface (record or otherwise) from a {@link net.phoenixvine.wiki.client.rich.markdown.BlockParser},
 * then registering a matching
 * {@link net.phoenixvine.wiki.client.rich.render.BlockRenderer} on
 * {@link net.phoenixvine.wiki.client.rich.render.BlockRendererRegistry#DEFAULT} so it knows how to
 * be measured and drawn. A block with no registered renderer is skipped rather than crashing.
 */
public interface RichBlock {

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

    record CollapsibleSection(int level, List<RichSpan> headingSpans, String collapseKey,
                              List<RichBlock> children) implements RichBlock {

        @Override
        public List<RichSpan> spans() {
            return headingSpans;
        }
    }

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
