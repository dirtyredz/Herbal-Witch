package io.github.dirtyredz.herbalwitch.companions.animals;

import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.*;
import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalProfiles;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.*;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.*;
import net.minecraftforge.registries.ForgeRegistries;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;
import static io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalCareAccess.Reason.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class AnimalCareGameTests {
    @GameTest(template = "empty", batch = "herbalwitch_animal_future_care")
    public static void futureCareSchemaCannotBeSedatedOrMutatedThroughSharedPermissions(GameTestHelper h) {
        floor(h); var player = AnimalFixtures.player(h, "m8_future_care"); var cow = AnimalFixtures.animal(h, EntityType.COW);
        var future = new net.minecraft.nbt.CompoundTag(); future.putInt("schema", 999); future.putString("ownerFormat", "unknown keeper authority");
        AnimalCapability.of(cow).orElseThrow().load(future); cow.setHealth(5);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(PreparationItems.product(EverydayProductCatalog.HEALING_SALVE)));
        check(AnimalCareService.heal(player, cow, InteractionHand.MAIN_HAND) == READ_ONLY && cow.getHealth() == 5 && player.getMainHandItem().getCount() == 1, "future care was mutated");
        check(!io.github.dirtyredz.herbalwitch.apothecary.brewing.application.ThrowableApplications.affect(
                io.github.dirtyredz.herbalwitch.apothecary.brewing.application.ThrowableApplications.Burst.SLEEP, player, cow), "unknown keeper authority was sedated");
        check(AnimalCapability.of(cow).orElseThrow().save().equals(future), "future data changed"); h.succeed();
    }
    @GameTest(template = "empty", batch = "herbalwitch_animal_feed_matrix")
    public static void eachRealSpeciesAcceptsOnlyItsMatchingBaitAndPersistsPrivateCooldown(GameTestHelper h) {
        floor(h); var player = AnimalFixtures.player(h, "m8_feed_matrix");
        for (var profile : AnimalProfiles.ALL) {
            Mob mob = (Mob) ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(profile.id())).create(h.getLevel());
            var pos = h.absolutePos(new net.minecraft.core.BlockPos(4, 1, 5)); mob.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 0, 0); h.getLevel().addFreshEntity(mob);
            Bait wrong = profile.bait() == Bait.GRAIN ? Bait.FISH : Bait.GRAIN;
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimalItems.bait(wrong), 3));
            check(AnimalCareService.feed(player, mob, InteractionHand.MAIN_HAND, wrong) == WRONG_BAIT, profile.id() + " accepted wrong bait");
            check(player.getMainHandItem().getCount() == 3 && AnimalCapability.of(mob).orElseThrow().state().isEmpty(), "denial mutated state");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimalItems.bait(profile.bait()), 3));
            check(AnimalCareService.feed(player, mob, InteractionHand.MAIN_HAND, profile.bait()) == READY, "matching feed refused " + profile.id());
            check(AnimalCareService.feed(player, mob, InteractionHand.MAIN_HAND, profile.bait()) == COOLDOWN && player.getMainHandItem().getCount() == 2, "cooldown consumed bait");
            check(AnimalTrustService.relationship(mob, player.getUUID()).trust() == 20, "wrong trust");
            check(AnimalTrustService.relationship(mob, new java.util.UUID(8, 8)).trust() == 0, "trust leaked to another player");
            var saved = new net.minecraft.nbt.CompoundTag(); mob.saveWithoutId(saved); Mob copy = (Mob) mob.getType().create(h.getLevel()); copy.load(saved);
            check(AnimalTrustService.relationship(copy, player.getUUID()).remaining(AnimalTrustService.clock(copy)) == 1200, "save/load lost cooldown");
            copy.discard(); mob.discard();
        }
        h.succeed();
    }
    @GameTest(template = "empty", batch = "herbalwitch_animal_damage")
    public static void successfulDamageRemovesOnlyAttackersTrustAndBlocksFeeding(GameTestHelper h) {
        floor(h); var player = AnimalFixtures.player(h, "m8_damage"); var other = AnimalFixtures.player(h, "m8_damage_other");
        var cow = AnimalFixtures.animal(h, EntityType.COW); AnimalFixtures.relationship(cow, player.getUUID(), 80, 4);
        check(AnimalTrustService.feed(cow, other.getUUID()), "other player did not earn personal trust");
        cow.hurt(player.damageSources().playerAttack(player), 1);
        check(AnimalTrustService.relationship(cow, player.getUUID()).trust() == 40, "attacker trust not reduced");
        check(AnimalTrustService.relationship(cow, other.getUUID()).trust() == 20, "other trust changed");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimalItems.bait(Bait.GRAIN)));
        check(AnimalCareService.feed(player, cow, InteractionHand.MAIN_HAND, Bait.GRAIN) == HURT, "hurt gate missing");
        check(player.getMainHandItem().getCount() == 1, "denied feed consumed bait"); h.succeed();
    }
    @GameTest(template = "empty", batch = "herbalwitch_animal_salve")
    public static void existingSalveHealsTargetReturnsBowlAndPreservesCreativeStack(GameTestHelper h) {
        floor(h); var player = AnimalFixtures.player(h, "m8_salve"); var cow = AnimalFixtures.animal(h, EntityType.COW);
        Item salve = PreparationItems.product(EverydayProductCatalog.HEALING_SALVE);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(salve));
        check(AnimalCareService.heal(player, cow, InteractionHand.MAIN_HAND) == HEALTH_FULL, "full health consumed salve");
        cow.setHealth(3); check(AnimalCareService.heal(player, cow, InteractionHand.MAIN_HAND) == READY, "salve failed");
        check(cow.getHealth() == 7 && player.getMainHandItem().is(Items.BOWL), "health or bowl wrong");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(salve));
        check(AnimalCareService.heal(player, cow, InteractionHand.MAIN_HAND) == COOLDOWN, "target healing cooldown missing");
        AnimalFixtures.advance(cow, 200); player.getAbilities().instabuild = true;
        check(AnimalCareService.heal(player, cow, InteractionHand.MAIN_HAND) == READY && player.getMainHandItem().is(salve), "creative salve was consumed");
        check(AnimalTrustService.relationship(cow, player.getUUID()).trust() == 0, "healing granted trust");
        player.getAbilities().instabuild = false; h.succeed();
    }
    @GameTest(template = "empty", batch = "herbalwitch_animal_click")
    public static void forgeEntityClickIsTerminalAndCannotRewardBothHands(GameTestHelper h) {
        floor(h); var player = AnimalFixtures.player(h, "m8_click"); var cow = AnimalFixtures.animal(h, EntityType.COW);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimalItems.bait(Bait.GRAIN), 2));
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(AnimalItems.bait(Bait.GRAIN), 2));
        var first = net.minecraftforge.common.ForgeHooks.onInteractEntity(player, cow, InteractionHand.MAIN_HAND);
        var second = net.minecraftforge.common.ForgeHooks.onInteractEntity(player, cow, InteractionHand.OFF_HAND);
        check(first != null && first.consumesAction() && second != null && second.consumesAction(), "fallthrough was not stopped");
        check(player.getMainHandItem().getCount() == 1 && player.getOffhandItem().getCount() == 2, "duplicate hand consumption");
        check(AnimalTrustService.relationship(cow, player.getUUID()).feeds() == 1 && !cow.isInLove(), "click bred or double-fed cow"); h.succeed();
    }
}
