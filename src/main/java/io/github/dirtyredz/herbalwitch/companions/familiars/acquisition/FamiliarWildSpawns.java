package io.github.dirtyredz.herbalwitch.companions.familiars.acquisition;

import io.github.dirtyredz.herbalwitch.companions.familiars.config.FamiliarServerConfig;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import java.util.List;

/** New wild acquisition uses passive spawn limits and safe ground, never a nocturnal calendar gate. */
public final class FamiliarWildSpawns {
    public record Wild(String species, int weight, TagKey<Biome> biomes) {}
    public static final TagKey<Biome> WOODED = tag("wooded"), WARM_DRY = tag("warm_dry");
    public static final List<Wild> ALL = List.of(new Wild("raven", 6, WOODED), new Wild("owl", 4, WOODED), new Wild("snake", 6, WARM_DRY));
    private FamiliarWildSpawns() {}
    private static TagKey<Biome> tag(String name) { return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("herbalwitch", "familiars/spawns/" + name)); }
    public static void register(SpawnPlacementRegisterEvent event) {
        for (var wild : ALL) event.register(FamiliarEntities.type(wild.species()), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> level.getLevel().dimension() == Level.OVERWORLD
                        && FamiliarServerConfig.current().species().contains(wild.species()) && level.hasChunkAt(pos)
                        && level.getRawBrightness(pos, 0) > 8 && level.getFluidState(pos).isEmpty()
                        && (level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON)
                        || wild.species().equals("snake") && level.getBlockState(pos.below()).is(BlockTags.SAND)),
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
