package io.github.dirtyredz.herbalwitch.data.server.botany;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.botany.conditions.HerbConditionReloadListener;
import io.github.dirtyredz.herbalwitch.apothecary.botany.conditions.HerbConditions;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Writes the catalogue default herb conditions to
 * {@code data/herbalwitch/herb_conditions/<group>/<herb>.json}, one file per herb.
 */
public class BotanyConditionProvider implements DataProvider {
    private final PackOutput.PathProvider paths;

    public BotanyConditionProvider(PackOutput output) {
        this.paths = output.createPathProvider(PackOutput.Target.DATA_PACK, HerbConditionReloadListener.DIRECTORY);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> writes = new ArrayList<>();
        for (HerbSpec herb : HerbCatalog.ALL) {
            Path path = paths.json(ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID,
                    herb.habitat().directory() + "/" + herb.id()));
            writes.add(DataProvider.saveStable(cache, HerbConditions.defaultFor(herb).toJson(), path));
        }
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Herbal Witch herb conditions";
    }
}
