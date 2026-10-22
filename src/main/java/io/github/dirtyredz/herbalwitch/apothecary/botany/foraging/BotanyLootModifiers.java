package io.github.dirtyredz.herbalwitch.apothecary.botany.foraging;

import com.mojang.serialization.Codec;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Global loot modifier serializers for foraging. The modifier instances are data (loot_modifiers/). */
public final class BotanyLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, HerbalWitch.MOD_ID);

    public static final RegistryObject<Codec<HerbForageModifier>> HERB_FORAGE =
            SERIALIZERS.register("botany/herb_forage", () -> HerbForageModifier.CODEC);

    private BotanyLootModifiers() {
    }
}
