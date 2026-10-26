package io.github.dirtyredz.herbalwitch.client.grimoire.guide;

import com.google.gson.JsonParser;
import guideme.compiler.PageCompiler;
import guideme.libs.mdast.mdx.model.MdxJsxElementFields;
import net.minecraft.resources.ResourceLocation;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.HerbalMaterialCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/** Validates authored navigation before any library filtering, against registry-owned catalogues and datagen states. */
class GuideResourcesTest {
    private static final Path ROOT = Path.of("src/generated/resources/assets/herbalwitch/guide");
    private static final Path GENERATED = Path.of("src/generated/resources");
    private static final Pattern ATTR = Pattern.compile("([\\w:]+)=(?:\"([^\"]*)\"|\\{([^}]+)})");

    @Test
    void jsxValuesAreTypedAndItemDestinationsAreDistinct() throws IOException {
        for (Path root : java.util.List.of(ROOT, Path.of("src/main/resources/assets/herbalwitch/guide_sources/en_us"))) {
            try (var files = Files.walk(root)) {
                for (Path file : files.filter(p -> p.toString().endsWith(".md")).toList()) {
                    String text = Files.readString(file);
                    var attrs = Pattern.compile("(?<![\\w:])([\\w:]+)=\"(true|false|-?\\d+(?:\\.\\d+)?)\"").matcher(text);
                    while (attrs.find()) assertTrue(attrs.group(1).startsWith("p:")
                            || Set.of("x", "y", "z").contains(attrs.group(1)), file + ": string-valued " + attrs.group());
                    var page = PageCompiler.parse("herbalwitch", new ResourceLocation("herbalwitch",
                            root.relativize(file).toString().replace('\\', '/')), text);
                    var items = (java.util.List<?>) page.getFrontmatter().additionalProperties().get("item_ids");
                    assertTrue(items == null || items.size() == 1, file + ": multiple items share a page-top destination");
                }
            }
        }
    }

    @Test
    void pinnedGuideMeParserRecognizesNativeFrontmatterScenesAndRecipeTags() throws IOException {
        var id = new ResourceLocation("herbalwitch", "apothecary/lavender.md");
        var page = PageCompiler.parse("herbalwitch", id, Files.readString(ROOT.resolve(id.getPath())));
        assertEquals("My first useful herb", page.getFrontmatter().navigationEntry().title());
        assertNull(page.getFrontmatter().additionalProperties().get("item_ids"));
        var tags = page.getAstRoot().children().stream().filter(MdxJsxElementFields.class::isInstance)
                .map(MdxJsxElementFields.class::cast).toList();
        assertEquals(5, tags.stream().filter(tag -> tag.name().equals("HerbalRecipe")).count());
        assertEquals(5, tags.stream().filter(tag -> tag.name().equals("HerbalCrafting")).count());
        var scenes = tags.stream().filter(tag -> tag.name().equals("GameScene")).toList();
        assertEquals(2, scenes.size());
        assertEquals(11, scenes.stream().flatMap(scene -> scene.children().stream())
                .filter(MdxJsxElementFields.class::isInstance).map(MdxJsxElementFields.class::cast)
                .filter(tag -> tag.name().equals("Block")).count());
    }

    @Test
    void allLinksResolveAndEveryPageIsReachableFromStart() throws IOException {
        Map<Path, Set<Path>> edges = new HashMap<>();
        try (var files = Files.walk(ROOT)) {
            for (Path page : files.filter(Files::isRegularFile).toList()) {
                var links = new HashSet<Path>();
                var match = Pattern.compile("\\[[^\\]]+\\]\\(([^)#]+)(?:#[^)]*)?\\)").matcher(Files.readString(page));
                while (match.find()) {
                    Path target = match.group(1).startsWith("herbalwitch:")
                            ? ROOT.resolve(match.group(1).substring(12))
                            : page.getParent().resolve(match.group(1)).normalize();
                    assertTrue(target.startsWith(ROOT) && Files.isRegularFile(target), page + " -> " + target);
                    links.add(target);
                }
                var parent = Pattern.compile("(?m)^  parent: herbalwitch:([^\\r\\n]+)$").matcher(Files.readString(page));
                if (parent.find()) assertTrue(Files.isRegularFile(ROOT.resolve(parent.group(1))), "Missing navigation parent");
                edges.put(page, links);
            }
        }
        Set<Path> reached = new HashSet<>(Set.of(ROOT.resolve("path/start.md")));
        while (true) {
            int before = reached.size();
            for (Path page : Set.copyOf(reached)) reached.addAll(edges.get(page));
            if (before == reached.size()) break;
        }
        assertEquals(edges.keySet(), reached);
        assertEquals(16, reached.size());
    }

    @Test
    void associationsNameRegisteredFormsAndStationsWithoutDuplicateRoutes() throws IOException {
        Set<String> registered = new HashSet<>();
        for (var herb : HerbCatalog.ALL) {
            registered.add("herbalwitch:" + herb.seedsPath());
            registered.add("herbalwitch:" + herb.freshPath());
            registered.add("herbalwitch:" + HerbalMaterialCatalog.bundlePath(herb));
            registered.add("herbalwitch:" + HerbalMaterialCatalog.powderPath(herb));
        }
        for (var station : StationCatalog.ALL) registered.add("herbalwitch:" + station.path());
        registered.add("herbalwitch:preparations/drinks/" + EverydayProductCatalog.RESTFUL_INFUSION.id());
        Set<String> associations = new HashSet<>();
        try (var pages = Files.walk(ROOT)) {
            for (Path page : pages.filter(Files::isRegularFile).toList()) {
                var match = Pattern.compile("(?m)^  - (herbalwitch:[a-z0-9_/]+)$").matcher(Files.readString(page));
                while (match.find()) {
                    String id = match.group(1);
                    assertTrue(registered.contains(id), "Unregistered association: " + id);
                    assertTrue(associations.add(id), "Duplicate association: " + id);
                    String path = id.substring(12);
                    String expected;
                    if (path.startsWith("stations/")) expected = path.replace("/station", ".md");
                    else if (id.equals("herbalwitch:" + EverydayProductCatalog.RESTFUL_INFUSION.path()))
                        expected = "apothecary/restful_infusion.md";
                    else {
                        String[] parts = path.split("/");
                        expected = "apothecary/" + parts[parts.length - 2] + "/" + parts[parts.length - 1] + ".md";
                    }
                    assertEquals(ROOT.resolve(expected), page, "Wrong item-specific target for " + id);
                    assertTrue(Files.isRegularFile(GENERATED.resolve("assets/herbalwitch/models/item/" + id.substring(12) + ".json")), id);
                }
            }
        }
        assertEquals(14, associations.size());
        assertTrue(associations.containsAll(Set.of("herbalwitch:botany/temperate/lavender/fresh",
                "herbalwitch:preparations/herbs/temperate/lavender/bundle",
                "herbalwitch:preparations/herbs/temperate/lavender/powder",
                "herbalwitch:stations/drying_rack/station", "herbalwitch:stations/mortar/station",
                "herbalwitch:stations/kettle/station", "herbalwitch:stations/stove/station")));
    }

    @Test
    void scenesUseExistingBlockStatesAndCompleteCatalogueLayouts() throws IOException {
        String page = Files.readString(ROOT.resolve("apothecary/lavender.md"));
        var scenes = Pattern.compile("<GameScene([^>]*)>(.*?)</GameScene>", Pattern.DOTALL).matcher(page);
        int sceneCount = 0;
        while (scenes.find()) {
            assertTrue(scenes.group(1).contains("interactive={true}"));
            Map<String, Map<Integer, String>> parts = new HashMap<>();
            Set<String> positions = new HashSet<>();
            var blocks = Pattern.compile("<Block ([^>]+)/>").matcher(scenes.group(2));
            int count = 0;
            while (blocks.find()) {
                count++;
                var attrs = attributes(blocks.group(1));
                String id = attrs.get("id");
                String position = attrs.get("x") + "," + attrs.get("y") + "," + attrs.get("z");
                assertTrue(positions.add(position), "Overlapping scene cells");
                if (id.equals("minecraft:stone")) continue;
                assertTrue(StationCatalog.ALL.stream().anyMatch(s -> id.equals("herbalwitch:" + s.path())), id);
                var states = JsonParser.parseString(Files.readString(GENERATED.resolve("assets/herbalwitch/blockstates/"
                        + id.substring(12) + ".json"))).getAsJsonObject();
                Map<String, String> requested = new HashMap<>();
                attrs.forEach((key, value) -> { if (key.startsWith("p:")) requested.put(key.substring(2), value); });
                if (states.has("variants")) {
                    assertTrue(states.getAsJsonObject("variants").keySet().stream()
                            .anyMatch(key -> state(key).entrySet().containsAll(requested.entrySet())), "Invalid scene state: " + attrs);
                } else {
                    Map<String, Set<String>> allowed = new HashMap<>();
                    for (var part : states.getAsJsonArray("multipart")) {
                        var when = part.getAsJsonObject().getAsJsonObject("when");
                        if (when != null) when.entrySet().forEach(entry -> allowed.computeIfAbsent(entry.getKey(), k -> new HashSet<>())
                                .add(entry.getValue().getAsString()));
                    }
                    requested.forEach((key, value) -> assertTrue(allowed.getOrDefault(key, Set.of()).contains(value), "Invalid scene state: " + attrs));
                }
                if (attrs.containsKey("p:part")) parts.computeIfAbsent(id, k -> new HashMap<>())
                        .put(Integer.parseInt(attrs.get("p:part")), position);
            }
            assertTrue(count > 0 && count <= 12);
            for (var entry : parts.entrySet()) {
                var spec = StationCatalog.ALL.stream().filter(s -> entry.getKey().equals("herbalwitch:" + s.path())).findFirst().orElseThrow();
                var cells = spec.layout().cells();
                assertEquals(cells.size(), entry.getValue().size());
                for (int i = 0; i < cells.size(); i++) {
                    var cell = cells.get(i);
                    assertEquals(-cell.right() + "," + (cell.up() + 1) + "," + cell.depth(), entry.getValue().get(i));
                }
            }
            sceneCount++;
        }
        assertEquals(2, sceneCount);
        assertTrue(page.contains("p:lit=\"true\""));
        var heat = JsonParser.parseString(Files.readString(GENERATED.resolve("data/herbalwitch/tags/blocks/heat_sources.json")));
        assertTrue(heat.toString().contains("herbalwitch:stations/stove/station"));
    }

    @Test
    void liveRecipeReferencesExistAndProseDoesNotCopyAmounts() throws IOException {
        String page = Files.readString(ROOT.resolve("apothecary/lavender.md"));
        var cards = Pattern.compile("<HerbalRecipe id=\"herbalwitch:([^\"]+)\" />").matcher(page);
        int count = 0;
        while (cards.find()) {
            assertTrue(Files.isRegularFile(GENERATED.resolve("data/herbalwitch/recipes/" + cards.group(1) + ".json")), cards.group(1));
            count++;
        }
        assertEquals(5, count);
        var crafting = Pattern.compile("<HerbalCrafting id=\"herbalwitch:([^\"]+)\" />").matcher(page);
        int craftingCount = 0;
        while (crafting.find()) {
            assertTrue(Files.isRegularFile(GENERATED.resolve("data/herbalwitch/recipes/" + crafting.group(1) + ".json")), crafting.group(1));
            craftingCount++;
        }
        assertEquals(5, craftingCount);
        String prose = page.replaceAll("(?s)<GameScene.*?</GameScene>", "");
        assertFalse(Pattern.compile("\\b\\d+\\s*(?:ticks|seconds|minutes|powders?|bottles?|energy)\\b").matcher(prose).find());
    }

    private static Map<String, String> attributes(String text) {
        Map<String, String> result = new HashMap<>();
        var matcher = ATTR.matcher(text);
        while (matcher.find()) result.put(matcher.group(1), matcher.group(2) != null ? matcher.group(2) : matcher.group(3));
        return result;
    }

    private static Map<String, String> state(String text) {
        Map<String, String> result = new HashMap<>();
        for (String value : text.split(",")) {
            var pair = value.split("=");
            if (pair.length == 2) result.put(pair[0], pair[1]);
        }
        return result;
    }
}
