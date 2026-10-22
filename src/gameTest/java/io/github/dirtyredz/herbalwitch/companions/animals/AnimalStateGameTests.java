package io.github.dirtyredz.herbalwitch.companions.animals;

import io.github.dirtyredz.herbalwitch.companions.animals.care.taming.AnimalOwnership;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalEligibility;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalProfiles;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalCapability;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.UUID;

@GameTestHolder("herbalwitch")
@PrefixGameTestTemplate(false)
public final class AnimalStateGameTests {
    @GameTest(template = "empty", batch = "herbalwitch_animal_state")
    public static void allSixteenRealTypesGetLazyStateAndExactProfiles(GameTestHelper helper) {
        for (var profile : AnimalProfiles.ALL) {
            var type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(profile.id()));
            Mob mob = (Mob) type.create(helper.getLevel());
            helper.assertTrue(mob != null, "real entity creation");
            var capability = AnimalCapability.of(mob).orElseThrow();
            helper.assertTrue(capability.state().isEmpty(), "empty provider has no care allocation: " + profile.id());
            helper.assertTrue(AnimalEligibility.profile(mob).orElseThrow().equals(profile), "live tags match profile");
            helper.assertTrue(capability.save().isEmpty(), "inspection has not allocated a relationship");
            mob.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "herbalwitch_animal_state")
    public static void unsupportedCapabilitySurvivesActualEntitySaveLoad(GameTestHelper helper) {
        Mob original = net.minecraft.world.entity.EntityType.COW.create(helper.getLevel());
        CompoundTag future = new CompoundTag(); future.putInt("schema", 90); future.putString("future", "preserve me");
        AnimalCapability.of(original).orElseThrow().load(future);
        CompoundTag saved = new CompoundTag(); original.saveWithoutId(saved);
        Mob restored = net.minecraft.world.entity.EntityType.COW.create(helper.getLevel());
        restored.load(saved);
        var capability = AnimalCapability.of(restored).orElseThrow();
        helper.assertTrue(!capability.writable(), "future schema remains read-only");
        helper.assertTrue(future.equals(capability.save()), "future fields survive real mob serialization");
        original.discard(); restored.discard(); helper.succeed();
    }

    @GameTest(template = "empty", batch = "herbalwitch_animal_state")
    public static void nativeOwnersRemainAuthoritativeWithoutFeedingHistory(GameTestHelper helper) {
        UUID owner = UUID.fromString("bde9fa65-e22c-4335-9c26-a706ef2d4808");
        Cat cat = net.minecraft.world.entity.EntityType.CAT.create(helper.getLevel());
        cat.setTame(true); cat.setOwnerUUID(owner);
        Horse horse = net.minecraft.world.entity.EntityType.HORSE.create(helper.getLevel());
        horse.setTamed(true); horse.setOwnerUUID(owner);
        for (Mob mob : new Mob[] {cat, horse}) {
            var view = AnimalOwnership.of(mob, AnimalEligibility.profile(mob).orElseThrow());
            helper.assertTrue(view.ownedBy(owner) && !view.allows(new UUID(0, 99)), "native pet stays owner-only");
            helper.assertTrue(AnimalCapability.of(mob).orElseThrow().state().isEmpty(), "no invented feeding history");
            mob.discard();
        }
        helper.succeed();
    }
}
