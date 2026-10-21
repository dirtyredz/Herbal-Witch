package io.github.dirtyredz.herbalwitch.brewing.product.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.brewing.effect.PestRepellentEffect;
import io.github.dirtyredz.herbalwitch.brewing.effect.WarmthEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The mod's own effects; every other product effect is a vanilla one. */
public final class PreparationEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, HerbalWitch.MOD_ID);

    public static final RegistryObject<MobEffect> WARMTH = EFFECTS.register("warmth", WarmthEffect::new);
    public static final RegistryObject<MobEffect> PEST_REPELLENT = EFFECTS.register("pest_repellent", PestRepellentEffect::new);

    private PreparationEffects() {
    }
}
