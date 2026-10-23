package io.github.dirtyredz.herbalwitch.companions.familiars.command;

import io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.*;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class FamiliarFlightGameTests {
    private static final Map<String, OnlinePlayers.Registration> SCOPES = new HashMap<>();
    private static void clean(String species) { var scope = SCOPES.remove(species); if (scope != null) scope.close(); }
    @AfterBatch(batch = "herbalwitch_familiar_raven_flight") public static void ravenCleanup(ServerLevel level) { clean("raven"); }
    @AfterBatch(batch = "herbalwitch_familiar_owl_flight") public static void owlCleanup(ServerLevel level) { clean("owl"); }
    @AfterBatch(batch = "herbalwitch_familiar_bat_flight") public static void batCleanup(ServerLevel level) { clean("bat"); }
    private static void flight(GameTestHelper h, String species) {
        FamiliarFixtures.bound(h, species, species + "_flight", bound -> {
            var owner = bound.owner(); var mob = bound.mob(); owner.getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get()));
            SCOPES.put(species, OnlinePlayers.register(List.of(owner)));
            owner.moveTo(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(1, 1, 1)))); mob.moveTo(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(7, 5, 7))));
            check(mob.getNavigation() instanceof FlyingPathNavigation, "flight adapter used ground navigation");
            float health = mob.getHealth(); check(!mob.causeFallDamage(8, 1, mob.damageSources().fall()) && mob.getHealth() == health, "flying familiar took a landing injury");
            double distance = mob.distanceToSqr(owner);
            check(FamiliarCommands.set(owner, bound.bond(), FamiliarCommand.FOLLOW).equals("command_set"), "flight follow refused");
            h.runAfterDelay(60, () -> {
                check(mob.distanceToSqr(owner) < distance - 16, "real flight made no progress: " + bound.record().state());
                check(mob.getHealth() == health && !mob.isNoAi(), "flight damaged or disabled the creature"); h.succeed();
            });
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_raven_flight", timeoutTicks = 120) public static void raven(GameTestHelper h) { flight(h, "raven"); }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_owl_flight", timeoutTicks = 120) public static void owl(GameTestHelper h) { flight(h, "owl"); }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_bat_flight", timeoutTicks = 120) public static void bat(GameTestHelper h) { flight(h, "bat"); }
}
