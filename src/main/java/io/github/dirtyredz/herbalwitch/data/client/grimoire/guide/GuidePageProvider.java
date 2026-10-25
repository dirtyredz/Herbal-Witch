package io.github.dirtyredz.herbalwitch.data.client.grimoire.guide;

import com.google.common.hash.Hashing;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.HerbalMaterialCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Authored prose plus catalogue-owned associations/scenes; recipe amounts never enter generated prose. */
public final class GuidePageProvider implements DataProvider {
    private final PackOutput output;

    public GuidePageProvider(PackOutput output) { this.output = output; }

    @Override
    public String getName() { return "Herbal Witch guide pages"; }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        try {
            write(cache, "path/start.md", source("path/start.md"));
            String lesson = source("apothecary/lavender.md");
            int frontmatterEnd = lesson.indexOf("\n---", 3);
            if (frontmatterEnd < 0) throw new IOException("Guide lesson has no frontmatter terminator");
            StringBuilder associations = new StringBuilder("\nitem_ids:\n");
            for (String item : items()) {
                if (!ForgeRegistries.ITEMS.containsKey(new ResourceLocation(item))) {
                    throw new IllegalStateException("Unknown guide item association: " + item);
                }
                associations.append("  - ").append(item).append('\n');
            }
            lesson = lesson.substring(0, frontmatterEnd) + associations.toString().stripTrailing() + lesson.substring(frontmatterEnd);
            lesson = lesson.replace("<!-- scene:drying_rack -->", GuideSceneProvider.dryingRack())
                    .replace("<!-- scene:heat -->", GuideSceneProvider.heat());
            write(cache, "apothecary/lavender.md", lesson);
            return CompletableFuture.completedFuture(null);
        } catch (IOException error) {
            return CompletableFuture.failedFuture(error);
        }
    }

    private static List<String> items() {
        List<String> ids = new ArrayList<>();
        for (var herb : List.of(HerbCatalog.LAVENDER, HerbCatalog.VALERIAN)) {
            ids.add(herb.seedsPath());
            ids.add(herb.freshPath());
            ids.add(HerbalMaterialCatalog.bundlePath(herb));
            ids.add(HerbalMaterialCatalog.powderPath(herb));
        }
        ids.add(EverydayProductCatalog.RESTFUL_INFUSION.path());
        for (var station : List.of(StationCatalog.DRYING_RACK, StationCatalog.MORTAR, StationCatalog.KETTLE,
                StationCatalog.STOVE, StationCatalog.HEARTH)) ids.add(station.path());
        return ids.stream().map(id -> "herbalwitch:" + id).toList();
    }

    private static String source(String page) throws IOException {
        String path = "/assets/herbalwitch/guide_sources/en_us/" + page;
        try (var stream = GuidePageProvider.class.getResourceAsStream(path)) {
            if (stream == null) throw new IOException("Missing guide source: " + path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        }
    }

    private void write(CachedOutput cache, String page, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        cache.writeIfNeeded(output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve("herbalwitch/guide/" + page), bytes, Hashing.sha1().hashBytes(bytes));
    }
}
