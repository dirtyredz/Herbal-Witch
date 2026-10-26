package io.github.dirtyredz.herbalwitch.data.client.grimoire.guide;

import com.google.common.hash.Hashing;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.GuideTopics;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
            write(cache, "apothecary/lavender.md", scenes(source("apothecary/lavender.md")));
            for (var topic : GuideTopics.FIRST_PREPARATION) {
                String item = topic.item();
                if (!ForgeRegistries.ITEMS.containsKey(new ResourceLocation(item))) {
                    throw new IllegalStateException("Unknown guide item association: " + item);
                }
                String page = source(topic.page());
                int end = page.indexOf("\n---", 3);
                if (end < 0) throw new IOException("Guide topic has no frontmatter terminator: " + topic.page());
                page = page.substring(0, end) + "\nitem_ids:\n  - " + item + page.substring(end);
                write(cache, topic.page(), scenes(page));
            }
            return CompletableFuture.completedFuture(null);
        } catch (IOException error) {
            return CompletableFuture.failedFuture(error);
        }
    }

    private static String scenes(String page) {
        return page.replace("{/* scene:drying_rack */}", GuideSceneProvider.dryingRack())
                .replace("{/* scene:heat */}", GuideSceneProvider.heat());
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
