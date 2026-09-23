package net.phoenixvine.wiki.client.rich;

import net.minecraft.resources.ResourceLocation;

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

    /**
     * {@code collapsible} defaults to true (headings fold into a {@link CollapsibleSection} by
     * {@link net.phoenixvine.wiki.client.rich.markdown.HeadingSectionGrouper} unless the source
     * tags the heading {@code {flat}}/{@code {nocollapse}}) -- see
     * {@link net.phoenixvine.wiki.client.rich.markdown.blocks.HeadingBlockParser}.
     */
    record Heading(int level, List<RichSpan> spans, boolean collapsible) implements RichBlock {

        public Heading(int level, List<RichSpan> spans) {
            this(level, spans, true);
        }
    }

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

    record Hotspot(int x, int y, String tooltip) {}

    /**
     * A fixed-size image with small always-visible hover-tooltip markers at pixel coordinates
     * relative to the image's top-left corner -- a labeled-diagram primitive. Parsed from a
     * {@code :::hotspots image/path,width,height} ... {@code @x,y tooltip text} ... {@code :::}
     * container (see {@link net.phoenixvine.wiki.client.rich.markdown.blocks.ContainerBlockParser}).
     */
    record HotspotImage(ResourceLocation image, int width, int height,
                        List<Hotspot> hotspots) implements RichBlock {

        @Override
        public List<RichSpan> spans() {
            return List.of();
        }
    }
}
