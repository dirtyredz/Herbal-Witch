package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.foraging;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.dirtyredz.herbalwitch.apothecary.botany.foraging.ForageEligibility;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationTags;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

/**
 * Resin, renewable from ordinary spruce trees: a player breaking resin-source leaves (not with
 * shears or Silk Touch) finds one resin at {@code chance} (1/8), on top of the vanilla drops.
 */
public class ResinDropModifier extends LootModifier {
    public static final Codec<ResinDropModifier> CODEC = RecordCodecBuilder.create(instance -> codecStart(instance)
            .and(Codec.doubleRange(0, 1).fieldOf("chance").forGetter(m -> m.chance))
            .apply(instance, ResinDropModifier::new));

    private final double chance;

    public ResinDropModifier(LootItemCondition[] conditions, double chance) {
        super(conditions);
        this.chance = chance;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (ForageEligibility.isPlayerHarvest(context, PreparationTags.RESIN_SOURCES) && context.getRandom().nextDouble() < chance) {
            loot.add(new ItemStack(PreparationItems.product(EverydayProductCatalog.RESIN)));
        }
        return loot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
