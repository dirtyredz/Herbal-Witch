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
            try {
                FamiliarServerConfig.install(new FamiliarRules(false, before.species(), 1, before.recordBytes(), before.transactionInterval(), before.scans(), before.paths(), before.guardRadius(), before.senseRadius(), before.offlineGuards(), before.publicRetrieval()));
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
}
