package io.github.dirtyredz.herbalwitch.magic.witch.energy;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/**
 * The world conditions that improve rest and meditation, read only at the player's own loaded
 * position (never loading anything): a biome in {@code herbalwitch:witch/energy/nature}, and a full
 * moon visible from where the player stands at night in a dimension with a normal day cycle. They
 * add to recovery only; nothing ever requires them.
 */
public final class RecoveryConditions {
    public static final TagKey<Biome> NATURE = TagKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "witch/energy/nature"));

    private RecoveryConditions() {
    }

    public static boolean inNature(ServerPlayer player) {
        Level level = player.level();
        BlockPos pos = player.blockPosition();
        return level.isLoaded(pos) && level.getBiome(pos).is(NATURE);
    }

    public static boolean fullMoonSky(ServerPlayer player) {
        Level level = player.level();
        BlockPos eyes = BlockPos.containing(player.getEyePosition());
        return level.isLoaded(eyes) && !level.dimensionType().hasFixedTime() && level.dimensionType().hasSkyLight()
                && level.getMoonPhase() == 0 && level.isNight() && level.canSeeSky(eyes);
    }

    /** The bonus percent for this player now, under the given balance. */
    public static int bonusPercent(ServerPlayer player, EnergyBalance balance) {
        return EnergyRecovery.bonusPercent(balance, inNature(player), fullMoonSky(player));
    }
}
