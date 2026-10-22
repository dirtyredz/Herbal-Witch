package io.github.dirtyredz.herbalwitch.companions.animals;

import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.animals.care.ai.*;
import io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalCareService;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalCapability;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationEffects;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.AdvancedProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.projectile.PreparationProjectile;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.ThrowableApplications.Burst;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;
import java.util.List;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;
import static io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalCareAccess.Reason.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class AnimalAiGameTests {
    @GameTest(template = "empty", batch = "herbalwitch_animal_calm")
    public static void calmPreemptsMovementWithoutRemovingNativePanicAndDamageEndsIt(GameTestHelper h) {
        floor(h); var player = AnimalFixtures.player(h, "m8_calm"); var cow = AnimalFixtures.animal(h, EntityType.COW);
        var nativeGoals = List.copyOf(cow.goalSelector.getAvailableGoals());
        try (var roster = OnlinePlayers.register(List.of(player))) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimalItems.CALMING.get(), 2));
            cow.setLastHurtByMob(player);
            check(AnimalCareService.calm(player, cow, InteractionHand.MAIN_HAND) == READY, "calm refused");
            cow.goalSelector.tick();
            check(cow.goalSelector.getAvailableGoals().containsAll(nativeGoals), "native goals removed");
            check(cow.goalSelector.getAvailableGoals().stream().anyMatch(g -> g.getGoal() instanceof AnimalCalmGoal && g.isRunning()), "calm not running");
            check(cow.getLastHurtByMob() == player, "attacker memory cleared");
            check(AnimalCareService.calm(player, cow, InteractionHand.MAIN_HAND) == ALREADY_CALM && player.getMainHandItem().getCount() == 1, "calm refreshed");
            cow.hurt(player.damageSources().playerAttack(player), 1);
            check(AnimalCapability.of(cow).orElseThrow().state().orElseThrow().calmRemaining() == 0, "damage did not end calm");
            check(AnimalCareService.calm(player, cow, InteractionHand.MAIN_HAND) == COOLDOWN, "damage reapplication lock missing");
        }
        h.succeed();
    }
    @GameTest(template = "empty", batch = "herbalwitch_animal_brain")
    public static void brainAndBatAiRemainUnmodifiedAndPowderConsumesNothing(GameTestHelper h) {
        floor(h); var player = AnimalFixtures.player(h, "m8_brain");
        for (var type : List.of(EntityType.GOAT, EntityType.AXOLOTL, EntityType.BAT)) {
            var mob = AnimalFixtures.animal(h, type); int before = mob.goalSelector.getAvailableGoals().size();
            AnimalGoalInstaller.reconcile(mob);
            check(mob.goalSelector.getAvailableGoals().size() == before, "special AI modified");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimalItems.CALMING.get()));
            check(AnimalCareService.calm(player, mob, InteractionHand.MAIN_HAND) == UNSUPPORTED && player.getMainHandItem().getCount() == 1, "unsupported powder consumed");
            mob.discard();
        }
        h.succeed();
    }
    @GameTest(template = "empty", batch = "herbalwitch_animal_sleep")
    public static void realSleepProjectileUsesM7WakeAndImmunity(GameTestHelper h) {
        floor(h); var player = AnimalFixtures.player(h, "m8_sleep"); var cow = AnimalFixtures.animal(h, EntityType.COW);
        class Impact extends PreparationProjectile {
            Impact() { super(h.getLevel(), player, Burst.SLEEP); setItem(new ItemStack(PreparationItems.product(AdvancedProductCatalog.SLEEP))); }
            void hit() { onHit(new net.minecraft.world.phys.EntityHitResult(cow)); }
        }
        var projectile = new Impact(); projectile.setPos(cow.position()); h.getLevel().addFreshEntity(projectile); projectile.hit();
        check(cow.hasEffect(PreparationEffects.SLEEP.get()) && projectile.isRemoved(), "projectile did not put cow to sleep");
        cow.hurt(player.damageSources().playerAttack(player), 1);
        check(!cow.hasEffect(PreparationEffects.SLEEP.get()), "damage did not wake cow");
        io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.control.ControlEvents.onLivingTick(new net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent(cow));
        check(cow.hasEffect(PreparationEffects.STEADFAST.get()), "M7 immunity missing"); h.succeed();
    }
}
