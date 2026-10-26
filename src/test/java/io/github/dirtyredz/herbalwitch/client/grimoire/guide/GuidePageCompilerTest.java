package io.github.dirtyredz.herbalwitch.client.grimoire.guide;

import guideme.GuidePage;
import guideme.PageCollection;
import guideme.color.SymbolicColor;
import guideme.compiler.AnchorIndexer;
import guideme.compiler.LinkParser;
import guideme.compiler.PageCompiler;
import guideme.compiler.ParsedGuidePage;
import guideme.compiler.TagCompiler;
import guideme.compiler.tags.MdxAttrs;
import guideme.document.block.LytBlockContainer;
import guideme.document.block.LytVisitor;
import guideme.document.flow.LytFlowContent;
import guideme.extensions.ExtensionCollection;
import guideme.indices.PageIndex;
import guideme.libs.mdast.mdx.model.MdxJsxAttribute;
import guideme.libs.mdast.mdx.model.MdxJsxElementFields;
import guideme.libs.mdast.mdx.model.MdxJsxFlowElement;
import guideme.libs.mdast.model.MdAstLink;
import guideme.libs.mdast.model.MdAstNode;
import guideme.libs.mdast.model.MdAstParent;
import guideme.libs.mdast.model.MdAstText;
import guideme.navigation.NavigationTree;
import io.github.dirtyredz.herbalwitch.client.grimoire.guide.animation.GuideAnimations;
import io.github.dirtyredz.herbalwitch.client.grimoire.guide.animation.WatchLessonTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/** Real pinned parser/compiler and anchor index; world/render-dependent tags use headless contracts. */
class GuidePageCompilerTest {
    private static final Path GENERATED = Path.of("src/generated/resources/assets/herbalwitch/guide");
    private static final Path AUTHORED = Path.of("src/main/resources/assets/herbalwitch/guide_sources/en_us");
    private static final ExtensionCollection EXTENSIONS = ExtensionCollection.builder()
            .add(TagCompiler.EXTENSION_POINT, new HeadlessTags()).build();

    /** Execute the real tag's absent branch, which must return before constructing client-only links. */
    @Test void absentPonderCompilesTheRealWatchTagWithoutEmittingContent() {
        for (String resource : List.of("net/createmod/ponder/api/registration/PonderPlugin.class",
                "net/createmod/catnip/gui/ScreenOpener.class", "dev/engine_room/flywheel/api/Flywheel.class")) {
            assertNull(getClass().getClassLoader().getResource(resource), "Optional library on test runtime: " + resource);
        }
        assertFalse(GuideAnimations.available(
                io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.GuideLessons.FIRST_PREPARATION));
        var id = new ResourceLocation("herbalwitch:test.md");
        var extensions = ExtensionCollection.builder()
                .add(TagCompiler.EXTENSION_POINT, new WatchLessonTag()).build();
        var parsed = PageCompiler.parse("test", id, "<HerbalWatch lesson=\"herbalwitch:first_preparation\" />");
        var document = PageCompiler.compile(new Pages(Map.of(id, parsed)), extensions, parsed).document();
        assertTrue(document.getTextContent().isBlank(), "Unavailable Watch must emit no label or error");
        document.visit(new LytVisitor() {
            @Override public Result beforeFlowContent(LytFlowContent content) {
                fail("Unavailable Watch emitted flow content: " + content);
                return Result.CONTINUE;
            }
        });
        var prose = PageCompiler.parse("test", id, "Written lesson remains readable.\n\n"
                + "<HerbalWatch lesson=\"herbalwitch:first_preparation\" />");
        assertEquals("Written lesson remains readable.", PageCompiler.compile(
                new Pages(Map.of(id, prose)), extensions, prose).document().getTextContent().trim());
    }

    @Test void everyAuthoredAndGeneratedPageHasValidMarkupLinksAndCompiledProse() throws Exception {
        var bundled = read(GENERATED);
        assertEquals(16, bundled.size(), "Every Phase 1 page must be generated");
        check(bundled);
        bundled.putAll(read(Path.of("src/main/resources/assets/herbalwitch/guide")));
        var authored = read(AUTHORED);
        assertEquals(bundled.keySet(), authored.keySet(), "Every authored page must be packaged");
        check(bundled);
        // Authored sources may link to generated-only pages; prefer authored versions where present.
        var sources = new HashMap<>(bundled);
        sources.putAll(authored);
        check(sources);
    }

    @Test void guardRejectsTheOwnerReportedAttributesAndDanglingAnchors() {
        var id = new ResourceLocation("herbalwitch:test.md");
        assertThrows(AssertionError.class, () -> check(Map.of(id,
                PageCompiler.parse("test", id, "<!-- scene:heat -->"))));
        check(Map.of(id, PageCompiler.parse("test", id, "{/* scene:heat */}")));
        assertThrows(AssertionError.class, () -> check(Map.of(id,
                PageCompiler.parse("test", id, "<GameScene interactive=\"true\" zoom=\"1.5\" />"))));
        check(Map.of(id, PageCompiler.parse("test", id,
                "<GameScene interactive={true} zoom={1.5}>\n<Block id=\"minecraft:stone\" x=\"1\" y=\"0\" z=\"0\" />\n</GameScene>")));
        assertThrows(AssertionError.class, () -> check(Map.of(id, PageCompiler.parse("test", id,
                "<GameScene interactive={true}>\n<Block id=\"minecraft:stone\" x={1} />\n</GameScene>"))));
        var missingAnchor = assertThrows(AssertionError.class, () -> check(Map.of(id,
                PageCompiler.parse("test", id, "# Existing\n\n[bad](test.md#absent)"))));
        assertTrue(missingAnchor.getMessage().contains("missing anchor"), missingAnchor.getMessage());
        assertThrows(AssertionError.class, () -> check(Map.of(id,
                PageCompiler.parse("test", id, "[bad](missing.md)"))));
        assertThrows(AssertionError.class, () -> check(Map.of(id,
                PageCompiler.parse("test", id, "<UnknownTag />"))));
        check(Map.of(id, PageCompiler.parse("test", id, "<HerbalWatch lesson=\"herbalwitch:first_preparation\" />")));
        for (String watch : List.of("<HerbalWatch />", "<HerbalWatch lesson=\"\" />", "<HerbalWatch lesson=\"Not An Id\" />",
                "<HerbalWatch lesson=\"first_preparation\" />", "<HerbalWatch lesson=\"herbalwitch:unknown\" />")) {
            assertThrows(AssertionError.class, () -> check(Map.of(id, PageCompiler.parse("test", id, watch))), watch);
        }
        assertThrows(AssertionError.class, () -> check(Map.of(id,
                PageCompiler.parse("test", id, "---\nitem_ids:\n  - herbalwitch:first\n  - herbalwitch:second\n---\n\n# Shared"))));
    }

    private static Map<ResourceLocation, ParsedGuidePage> read(Path root) throws Exception {
        Map<ResourceLocation, ParsedGuidePage> pages = new HashMap<>();
        if (!Files.isDirectory(root)) return pages;
        try (var files = Files.walk(root)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".md")).toList()) {
                var id = new ResourceLocation("herbalwitch", root.relativize(file).toString().replace('\\', '/'));
                pages.put(id, PageCompiler.parse("test", id, Files.readString(file)));
            }
        }
        return pages;
    }

    private static void check(Map<ResourceLocation, ParsedGuidePage> pages) {
        var collection = new Pages(pages);
        Map<ResourceLocation, List<String>> links = new HashMap<>();
        for (var page : pages.values()) {
            var targets = new ArrayList<String>();
            links.put(page.getId(), targets);
            assertFalse(page.getAstRoot().toText().contains("PARSING ERROR"),
                    () -> page.getId() + ": " + page.getAstRoot().toText());
            walk(page.getAstRoot(), node -> {
                if (node instanceof MdAstLink link) targets.add(link.url());
                if (node instanceof MdxJsxElementFields tag) {
                    for (var attribute : tag.attributes()) {
                        if (attribute instanceof MdxJsxAttribute attr && !(tag.name().equals("Block")
                                && (attr.name.startsWith("p:") || Set.of("x", "y", "z").contains(attr.name)))) {
                            assertFalse(attr.hasStringValue() && attr.getStringValue().matches("true|false|-?\\d+(?:\\.\\d+)?"),
                                    page.getId() + ": string-valued JSX attribute " + attr.name);
                        }
                    }
                }
            });
            // LytFlowLink initializes GuideMEClient and Forge registries, which require a transformed
            // game process. Validate links with the real LinkParser below; compile their labels as text.
            unwrapLinks(page.getAstRoot());
            var compiled = collection.getPage(page.getId());
            compiled.document().visit(new LytVisitor() {
                @Override public Result beforeFlowContent(LytFlowContent content) {
                    assertNotEquals(SymbolicColor.ERROR_TEXT, content.getStyle().color(),
                            page.getId() + ": " + compiled.document().getTextContent());
                    return Result.CONTINUE;
                }
            });
        }
        Set<String> items = new HashSet<>();
        for (var page : pages.values()) {
            var associations = (List<?>) page.getFrontmatter().additionalProperties().get("item_ids");
            if (associations != null) {
                assertEquals(1, associations.size(), page.getId() + ": item targets must be distinct");
                for (var item : associations) assertTrue(items.add((String) item), "Duplicate association " + item);
            }
            for (String url : links.get(page.getId())) resolve(collection, page.getId(), url);
        }
    }

    private static void resolve(Pages pages, ResourceLocation from, String url) {
        var compiler = new PageCompiler(pages, EXTENSIONS, "test", from, "");
        LinkParser.parseLink(compiler, url, new LinkParser.Visitor() {
            public void handleExternal(java.net.URI uri) { }
            public void handleError(String error) { fail(from + " -> " + url + ": " + error); }
            public void handlePage(guideme.PageAnchor target) {
                if (target.anchor() != null) assertNotNull(new AnchorIndexer(pages.getPage(target.pageId()).document())
                        .get(target.anchor()), from + " -> missing anchor " + url);
            }
        });
    }

    private static void unwrapLinks(MdAstNode node) {
        if (node instanceof MdAstParent<?> parent) {
            for (var child : List.copyOf(parent.children())) {
                if (child instanceof MdAstLink link) {
                    var text = new MdAstText();
                    text.setValue(link.toText());
                    parent.replaceChild(link, text);
                } else unwrapLinks((MdAstNode) child);
            }
        } else if (node instanceof MdxJsxElementFields element) {
            for (var child : element.children()) unwrapLinks((MdAstNode) child);
        }
    }

    private static void walk(MdAstNode node, Consumer<MdAstNode> visitor) {
        visitor.accept(node);
        if (node instanceof MdAstParent<?> parent) {
            for (var child : parent.children()) walk((MdAstNode) child, visitor);
        } else if (node instanceof MdxJsxElementFields element) {
            for (var child : element.children()) walk((MdAstNode) child, visitor);
        }
    }

    private static final class Pages implements PageCollection {
        private final Map<ResourceLocation, ParsedGuidePage> parsed;
        private final Map<ResourceLocation, GuidePage> compiled = new HashMap<>();
        Pages(Map<ResourceLocation, ParsedGuidePage> parsed) { this.parsed = parsed; }
        public <T extends PageIndex> T getIndex(Class<T> type) { throw new AssertionError("Unexpected live index " + type); }
        public Collection<ParsedGuidePage> getPages() { return parsed.values(); }
        public ParsedGuidePage getParsedPage(ResourceLocation id) { return parsed.get(id); }
        public GuidePage getPage(ResourceLocation id) {
            return compiled.computeIfAbsent(id, key -> PageCompiler.compile(this, EXTENSIONS, parsed.get(key)));
        }
        public byte[] loadAsset(ResourceLocation id) { throw new AssertionError("Unexpected asset " + id); }
        public NavigationTree getNavigationTree() { throw new AssertionError("Unexpected navigation tree"); }
        public boolean pageExists(ResourceLocation id) { return parsed.containsKey(id); }
    }

    /** Native scene compilation needs Minecraft.level/model rendering, unavailable in a headless JUnit JVM. */
    private static final class HeadlessTags implements TagCompiler {
        public Set<String> getTagNames() { return Set.of("GameScene", "HerbalRecipe", "HerbalCrafting", "Block", WatchLessonTag.TAG); }
        public void compileBlockContext(PageCompiler compiler, LytBlockContainer parent, MdxJsxFlowElement tag) {
            validate(compiler, parent, tag);
        }
        private void validate(PageCompiler compiler, LytBlockContainer parent, MdxJsxElementFields tag) {
            switch (tag.name()) {
                case "GameScene" -> {
                    MdxAttrs.getBoolean(compiler, parent, tag, "interactive", false);
                    MdxAttrs.getFloat(compiler, parent, tag, "zoom", 1);
                    MdxAttrs.getBoolean(compiler, parent, tag, "fullWidth", false);
                    for (var child : tag.children()) {
                        if (child instanceof MdxJsxElementFields nested) validate(compiler, parent, nested);
                    }
                }
                case "Block" -> {
                    MdxAttrs.getRequiredId(compiler, parent, tag, "id");
                    MdxAttrs.getPos(compiler, parent, tag);
                }
                case "HerbalRecipe", "HerbalCrafting" -> {
                    var id = MdxAttrs.getRequiredId(compiler, parent, tag, "id");
                    if (id != null) assertTrue(Files.isRegularFile(Path.of("src/generated/resources/data",
                            id.getNamespace(), "recipes", id.getPath() + ".json")), "Missing recipe " + id);
                }
                case WatchLessonTag.TAG -> GuideAnimations.problem(MdxAttrs.getString(compiler, parent, tag,
                        WatchLessonTag.LESSON_ATTRIBUTE, null)).ifPresent(problem -> parent.appendError(compiler, problem, tag));
                default -> fail("Unsupported nested tag " + tag.name());
            }
        }
    }
}
