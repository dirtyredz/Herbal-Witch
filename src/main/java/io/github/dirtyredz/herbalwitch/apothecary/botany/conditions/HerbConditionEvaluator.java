package io.github.dirtyredz.herbalwitch.apothecary.botany.conditions;

import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbHabitat.Preference;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;

/**
 * Reads live world conditions against the current {@link HerbConditions}: biome habitat, rain at
 * the spot, a covered sky, night and full moon. Worldgen never calls this; weather and moon only
 * matter for live foraging and harvests.
 */
public final class HerbConditionEvaluator {
    private HerbConditionEvaluator() {
    }

    public static boolean matches(ServerLevel level, BlockPos pos, Preference preference) {
        return switch (preference) {
            case RAIN -> level.isRainingAt(pos.above()) || level.isRainingAt(pos);
            case LOW_SKY -> !level.canSeeSky(pos.above());
            case NIGHT -> level.isNight();
            case FULL_MOON -> level.isNight() && level.getMoonBrightness() >= 1.0F;
        };
    }

    public static int matched(ServerLevel level, BlockPos pos, HerbConditions.Condition condition) {
        int matched = 0;
        for (Preference preference : condition.preferences()) {
            if (matches(level, pos, preference)) {
                matched++;
            }
        }
        return matched;
    }

    public static boolean inHabitat(ServerLevel level, BlockPos pos, HerbConditions.Condition condition) {
        TagKey<Biome> tag = TagKey.create(Registries.BIOME, ResourceLocation.parse(condition.habitat()));
        return level.getBiome(pos).is(tag);
    }

    /** In its habitat biome, or at least one liked condition met. */
    public static boolean favoured(ServerLevel level, BlockPos pos, HerbConditions.Condition condition) {
        return inHabitat(level, pos, condition) || matched(level, pos, condition) > 0;
    }

    /** 0 or 1 extra fresh herb for a harvest at {@code pos}; never takes anything away. */
    public static int harvestBonus(ServerLevel level, BlockPos pos, HerbSpec herb, RandomSource random) {
        HerbConditions.Condition condition = HerbConditionReloadListener.current().get(herb);
        if (condition == null || !favoured(level, pos, condition)) {
            return 0;
        }
        return random.nextDouble() < condition.harvestBonusChance() ? 1 : 0;
    }
}
