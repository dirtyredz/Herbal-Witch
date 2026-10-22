package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.projectile.PreparationProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The thrown preparation entity, {@code herbalwitch:brewing/preparation_projectile}. */
public final class BrewingEntities {
    public static final String PROJECTILE_PATH = "brewing/preparation_projectile";
    public static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HerbalWitch.MOD_ID);

    public static final RegistryObject<EntityType<PreparationProjectile>> PREPARATION_PROJECTILE = TYPES.register(PROJECTILE_PATH,
            () -> EntityType.Builder.<PreparationProjectile>of(PreparationProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10).build(HerbalWitch.MOD_ID + ":" + PROJECTILE_PATH));

    private BrewingEntities() {
    }
}
