package io.github.dirtyredz.herbalwitch.apothecary.botany.foraging;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.botany.conditions.HerbConditionEvaluator;
import io.github.dirtyredz.herbalwitch.apothecary.botany.conditions.HerbConditionReloadListener;
import io.github.dirtyredz.herbalwitch.apothecary.botany.conditions.HerbConditions;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyTags;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Herb seeds from grass and ferns broken by a player ({@link ForageEligibility}). Adds to the
 * vanilla drops, never replaces them. Two independent rolls:
 * <ul>
 *   <li>starter: {@code starter_chance} (1/16) for seeds of one of the four starter herbs, anywhere;</li>
 *   <li>discovery: one herb whose habitat contains this biome is picked, and its seeds drop at its
 *       condition's discovery chance, doubled per liked condition met, capped at 1/8.</li>
 * </ul>
 */
public class HerbForageModifier extends LootModifier {
    public static final Codec<HerbForageModifier> CODEC = RecordCodecBuilder.create(instance -> codecStart(instance)
            .and(Codec.doubleRange(0, 1).fieldOf("starter_chance").forGetter(m -> m.starterChance))
            .apply(instance, HerbForageModifier::new));

    private final double starterChance;

    public HerbForageModifier(LootItemCondition[] conditions, double starterChance) {
        super(conditions);
        this.starterChance = starterChance;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (!ForageEligibility.isPlayerHarvest(context, BotanyTags.FORAGE_SOURCES)) {
            return loot;
        }
        RandomSource random = context.getRandom();
        if (random.nextDouble() < starterChance) {
            HerbSpec starter = HerbCatalog.STARTERS.get(random.nextInt(HerbCatalog.STARTERS.size()));
            loot.add(new ItemStack(BotanyItems.seeds(starter)));
        }
        Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
        if (origin != null) {
            discover(context.getLevel(), BlockPos.containing(origin), random, loot);
        }
        return loot;
    }

    private static void discover(ServerLevel level, BlockPos pos, RandomSource random, List<ItemStack> loot) {
        HerbConditions conditions = HerbConditionReloadListener.current();
        List<HerbConditions.Condition> candidates = new ArrayList<>();
        List<HerbSpec> herbs = new ArrayList<>();
        for (HerbSpec herb : HerbCatalog.ALL) {
            HerbConditions.Condition condition = conditions.get(herb);
            if (condition != null && HerbConditionEvaluator.inHabitat(level, pos, condition)) {
                candidates.add(condition);
                herbs.add(herb);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        int pick = random.nextInt(candidates.size());
        HerbConditions.Condition condition = candidates.get(pick);
        double chance = condition.discoveryChance(HerbConditionEvaluator.matched(level, pos, condition));
        if (random.nextDouble() < chance) {
            loot.add(new ItemStack(BotanyItems.seeds(herbs.get(pick))));
        }
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
