package io.github.dirtyredz.herbalwitch.brewing.product.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.brewing.effect.PestRepellentEffect;
import io.github.dirtyredz.herbalwitch.brewing.effect.PlainEffect;
import io.github.dirtyredz.herbalwitch.brewing.effect.WarmthEffect;
import io.github.dirtyredz.herbalwitch.brewing.effect.control.HeldEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/**
 * The mod's own effects; every other product effect is a vanilla one. Milestone 7's are
 * {@code brewing/effects/<effect>}: the two hard controls, the immunity that follows them, the
 * acquisition filters (concealment, dazzled, the two family repellents) and thorns.
 */
public final class PreparationEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, HerbalWitch.MOD_ID);

    public static final RegistryObject<MobEffect> WARMTH = EFFECTS.register("warmth", WarmthEffect::new);
    public static final RegistryObject<MobEffect> PEST_REPELLENT = EFFECTS.register("pest_repellent", PestRepellentEffect::new);

    public static final RegistryObject<MobEffect> BINDING = EFFECTS.register("brewing/effects/binding",
            () -> new HeldEffect(0x6B7A3A, false, "5c1b6f2e-3a7d-4b9e-8a51-2f4c0e9d7b11", "5c1b6f2e-3a7d-4b9e-8a51-2f4c0e9d7b12"));
    public static final RegistryObject<MobEffect> SLEEP = EFFECTS.register("brewing/effects/sleep",
            () -> new HeldEffect(0x9A86C8, true, "5c1b6f2e-3a7d-4b9e-8a51-2f4c0e9d7b21", "5c1b6f2e-3a7d-4b9e-8a51-2f4c0e9d7b22"));
    public static final RegistryObject<MobEffect> STEADFAST = EFFECTS.register("brewing/effects/steadfast",
            () -> new PlainEffect(MobEffectCategory.NEUTRAL, 0xC9B98A));
    public static final RegistryObject<MobEffect> CONCEALMENT = EFFECTS.register("brewing/effects/concealment",
            () -> new PlainEffect(MobEffectCategory.BENEFICIAL, 0x707078));
    public static final RegistryObject<MobEffect> DAZZLED = EFFECTS.register("brewing/effects/dazzled",
            () -> new PlainEffect(MobEffectCategory.HARMFUL, 0xE8E0C8));
    public static final RegistryObject<MobEffect> THORNS = EFFECTS.register("brewing/effects/thorns",
            () -> new PlainEffect(MobEffectCategory.BENEFICIAL, 0x8A2A3A));
    public static final RegistryObject<MobEffect> UNDEAD_REPELLENT = EFFECTS.register("brewing/effects/undead_repellent",
            () -> new PlainEffect(MobEffectCategory.BENEFICIAL, 0xE8E2D0));
    public static final RegistryObject<MobEffect> ARTHROPOD_REPELLENT = EFFECTS.register("brewing/effects/arthropod_repellent",
            () -> new PlainEffect(MobEffectCategory.BENEFICIAL, 0xC08A30));

    /** Milestone 7's effects, in registration order (names and manifests). */
    public static List<RegistryObject<MobEffect>> advanced() {
        return List.of(BINDING, SLEEP, STEADFAST, CONCEALMENT, DAZZLED, THORNS, UNDEAD_REPELLENT, ARTHROPOD_REPELLENT);
    }

    private PreparationEffects() {
    }
}
