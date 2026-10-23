package io.github.dirtyredz.herbalwitch.companions.familiars.config;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.FamiliarEligibility;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class FamiliarConfigGameTests {
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_config", timeoutTicks = 100)
    public static void capReloadKeepsOldestUnloadedReservationsAndBindingSwitchPreservesExistingBonds(GameTestHelper h) {
        FamiliarFixtures.bound(h, "cat", "config", bound -> {
            var owner = bound.owner(); owner.getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get()));
            var store = FamiliarStore.get(owner.server); List<UUID> bonds = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                var record = new FamiliarRecord(UUID.randomUUID(), owner.getUUID(), UUID.randomUUID(), "cat", "minecraft:cat", 1,
                        FamiliarRecord.Phase.ACTIVE, UUID.randomUUID(), 1, bound.record().state(), new CompoundTag());
                check(store.create(record), "unloaded fixture"); bonds.add(record.bond());
                check(FamiliarCommands.set(owner, record.bond(), FamiliarCommand.FOLLOW).equals("command_set"), "reserve slot");
            }
            var before = FamiliarServerConfig.current();
            try (var online = io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers.register(List.of(owner))) {
                var task = new io.github.dirtyredz.herbalwitch.companions.familiars.ability.sensing.FamiliarSenseTask(bound.mob(), io.github.dirtyredz.herbalwitch.companions.familiars.ability.FamiliarPower.THREATS, new io.github.dirtyredz.herbalwitch.companions.familiars.ability.sensing.FamiliarHerbCursor());
                check(task.valid(), "initial queued sensing invalid");
                var targetPos = bound.mob().blockPosition().below();
                var targets = io.github.dirtyredz.herbalwitch.companions.familiars.observation.FamiliarObservationsStore.class;
                check(io.github.dirtyredz.herbalwitch.companions.familiars.observation.FamiliarObservationsStore.publish(owner, bound.mob(), List.of(new io.github.dirtyredz.herbalwitch.companions.familiars.observation.FamiliarObservationsStore.Target(null,targetPos,h.getLevel().getBlockState(targetPos),"herb","test"))), "observation setup");
                UUID token = observedToken(owner.getUUID(),bound.bond());
                check(io.github.dirtyredz.herbalwitch.companions.familiars.observation.FamiliarObservationsStore.target(owner,bound.mob(),token).isPresent(), "initial observation invalid");
                FamiliarServerConfig.install(new FamiliarRules(false, before.species(), 1, before.recordBytes(), before.transactionInterval(), before.scans(), before.paths(), before.guardRadius(), before.senseRadius(), before.offlineGuards(), before.publicRetrieval()));
                check(!task.valid(), "config reload retained queued work");
                check(io.github.dirtyredz.herbalwitch.companions.familiars.observation.FamiliarObservationsStore.target(owner,bound.mob(),token).isEmpty(), "config reload retained action token");
                var untouched = bound.record(); task.perform(); check(bound.record().equals(untouched), "stale task mutated bond");
                FamiliarCommands.reconcileCap(owner.server);
                check(store.mobile().assignments(owner.getUUID()).equals(List.of(bonds.get(0))), "cap reload did not retain oldest");
                for (int i = 1; i < 3; i++) check(store.record(bonds.get(i)).orElseThrow().command() == FamiliarCommand.STAY, "excess reservation not released");
                check(FamiliarCommands.set(owner, bound.bond(), FamiliarCommand.PASSIVE).equals("command_set"), "acquisition switch disabled existing bond");
                var source = h.spawn(EntityType.CAT, 4, 1, 6);
                check(FamiliarEligibility.refusal(owner, source, h.absolutePos(new BlockPos(4, 1, 4))).equals("disabled"), "new binding switch ignored"); source.discard();
            } finally { FamiliarServerConfig.install(before); FamiliarCommands.reconcileCap(owner.server); }
            h.succeed();
        });
    }
    /** Inspect the server-created opaque token without exposing a production enumeration API. */
    private static UUID observedToken(UUID owner, UUID bond) {
        try {
            var field=io.github.dirtyredz.herbalwitch.companions.familiars.observation.FamiliarObservationsStore.class.getDeclaredField("OWNERS"); field.setAccessible(true);
            var pages=(java.util.Map<?,?>)((java.util.Map<?,?>)field.get(null)).get(owner); var page=pages.get(bond);
            var accessor=page.getClass().getDeclaredMethod("targets"); accessor.setAccessible(true);
            return (UUID)((java.util.Map<?,?>)accessor.invoke(page)).keySet().iterator().next();
        } catch(ReflectiveOperationException failure) { throw new AssertionError(failure); }
    }
}