package io.github.dirtyredz.herbalwitch.magic.broom.registry;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.broom.entity.BroomEntity;
import io.github.dirtyredz.herbalwitch.magic.broom.catalog.BroomSpec;
import net.minecraft.world.entity.*;
import net.minecraftforge.registries.*;
public final class BroomEntities {
 public static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,HerbalWitch.MOD_ID);
 public static final RegistryObject<EntityType<BroomEntity>> BROOM=TYPES.register("magic/broom/broom",()->EntityType.Builder.<BroomEntity>of(BroomEntity::new,MobCategory.MISC).sized(BroomSpec.WIDTH,BroomSpec.HEIGHT).clientTrackingRange(10).updateInterval(1).build("herbalwitch:magic/broom/broom"));
 private BroomEntities(){}
}
