package io.github.dirtyredz.herbalwitch.companions.familiars.registry;

import io.github.dirtyredz.herbalwitch.companions.familiars.species.ground.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.species.flying.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.*;
import java.util.Map;

public final class FamiliarEntities {
    public static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "herbalwitch");
    public static final RegistryObject<EntityType<FamiliarCat>> CAT = type("cat", FamiliarCat::new, .6f, .7f);
    public static final RegistryObject<EntityType<FamiliarRaven>> RAVEN = type("raven", FamiliarRaven::new, .5f, .7f);
    public static final RegistryObject<EntityType<FamiliarOwl>> OWL = type("owl", FamiliarOwl::new, .5f, .7f);
    public static final RegistryObject<EntityType<FamiliarFox>> FOX = type("fox", FamiliarFox::new, .6f, .7f);
    public static final RegistryObject<EntityType<FamiliarWolf>> WOLF = type("wolf", FamiliarWolf::new, .6f, .85f);
    public static final RegistryObject<EntityType<FamiliarSnake>> SNAKE = type("snake", FamiliarSnake::new, .5f, .25f);
    public static final RegistryObject<EntityType<FamiliarRabbit>> RABBIT = type("rabbit", FamiliarRabbit::new, .4f, .5f);
    public static final RegistryObject<EntityType<FamiliarBat>> BAT = type("bat", FamiliarBat::new, .5f, .9f);
    private static final Map<String, RegistryObject<? extends EntityType<? extends Mob>>> BY_NAME = Map.of("cat", CAT, "raven", RAVEN, "owl", OWL, "fox", FOX, "wolf", WOLF, "snake", SNAKE, "rabbit", RABBIT, "bat", BAT);
    private FamiliarEntities() {}
    private static <T extends Mob> RegistryObject<EntityType<T>> type(String key, EntityType.EntityFactory<T> factory, float width, float height) {
        return TYPES.register("familiars/entity/" + key, () -> EntityType.Builder.of(factory, MobCategory.CREATURE).sized(width, height).clientTrackingRange(8).build("herbalwitch:familiars/entity/" + key));
    }
    public static EntityType<? extends Mob> type(String key) { return BY_NAME.get(key).get(); }
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(CAT.get(), Cat.createAttributes().build()); event.put(FOX.get(), Fox.createAttributes().build());
        event.put(WOLF.get(), Wolf.createAttributes().build()); event.put(RABBIT.get(), Rabbit.createAttributes().build());
        event.put(BAT.get(), Bat.createAttributes().add(Attributes.FLYING_SPEED, .4).build());
        for (var bird : java.util.List.of(RAVEN, OWL)) event.put(bird.get(), Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10).add(Attributes.MOVEMENT_SPEED, .25).add(Attributes.FLYING_SPEED, .4).build());
        event.put(SNAKE.get(), Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10).add(Attributes.MOVEMENT_SPEED, .25).add(Attributes.ATTACK_DAMAGE, 2).build());
    }
}
