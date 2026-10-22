package io.github.dirtyredz.herbalwitch.decor.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.behavior.seating.SeatEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Decor's entity types: the invisible seat a sitter rides (never saved, never summoned by command). */
public final class DecorEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HerbalWitch.MOD_ID);

    public static final RegistryObject<EntityType<SeatEntity>> SEAT = ENTITIES.register("decor/seat",
            () -> EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC).sized(0.001F, 0.001F).noSave().noSummon()
                    .clientTrackingRange(10).updateInterval(Integer.MAX_VALUE).build(HerbalWitch.MOD_ID + ":decor/seat"));

    private DecorEntities() {
    }
}
