package io.github.dirtyredz.herbalwitch.data.server.loot;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.foraging.HerbForageModifier;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.foraging.ResinDropModifier;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

/**
 * The only writer of global loot modifiers: every feature's additive drops land in one
 * {@code data/forge/loot_modifiers/global_loot_modifiers.json}, which two providers would overwrite.
 */
public class HerbalWitchLootModifierProvider extends GlobalLootModifierProvider {
    /** Starter herb seeds per player-broken grass or fern. */
    public static final double STARTER_CHANCE = 1.0 / 16;
    /** Resin per player-broken spruce leaves. */
    public static final double RESIN_CHANCE = 1.0 / 8;

    public HerbalWitchLootModifierProvider(PackOutput output) {
        super(output, HerbalWitch.MOD_ID);
    }

    @Override
    protected void start() {
        add("botany/herb_forage", new HerbForageModifier(new LootItemCondition[0], STARTER_CHANCE));
        add("preparations/resin", new ResinDropModifier(new LootItemCondition[0], RESIN_CHANCE));
    }
}
