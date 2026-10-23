package io.github.dirtyredz.herbalwitch.companions.familiars.home;

import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarHomeKind;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures;
import io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarRosterMenu;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarHomes;
import io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.gametest.*;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class FamiliarHomeGameTests {
    private static OnlinePlayers.Registration returning;
    @AfterBatch(batch = "herbalwitch_familiar_home_return") public static void cleanup(ServerLevel level) { if (returning != null) { returning.close(); returning = null; } }
    private static FamiliarHomeBlockEntity home(GameTestHelper h, FamiliarHomeKind kind, BlockPos pos, net.minecraft.server.level.ServerPlayer owner) {
        check(PlayerFixtures.placeOnFloor(h, owner, FamiliarHomes.get(kind).item().get(), pos).consumesAction(), "home placement failed");
        return (FamiliarHomeBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(pos));
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_home_placement", timeoutTicks = 80)
    public static void actualPlacementUsesFloorSupportAndDropsOneEmptyItem(GameTestHelper h) {
        floor(h); var owner = AnimalFixtures.player(h, "m9_home_placement"); owner.moveTo(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4,1,1))));
        int x = 2;
        for (var kind : FamiliarHomeKind.values()) {
            var pos = new BlockPos(x++, 1, 4); var entry = FamiliarHomes.get(kind); var stack = new ItemStack(entry.item().get(), 2);
            check(PlayerFixtures.useItemOn(h, owner, stack, pos.below(), Direction.UP).consumesAction() && stack.getCount() == 1, "placement consumption");
            var home = (FamiliarHomeBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(pos));
            check(home != null && owner.getUUID().equals(home.owner()), "placement lost owner");
            check(!h.getBlockState(pos).canOcclude() && !h.getBlockState(pos).getShape(h.getLevel(), h.absolutePos(pos)).isEmpty(), "home shape/occlusion");
            var saved = home.saveWithFullMetadata(); var restored = new FamiliarHomeBlockEntity(home.getBlockPos(), home.getBlockState()); restored.load(saved);
            check(restored.instance().equals(home.instance()) && restored.owner().equals(owner.getUUID()), "identity changed on NBT reload");
            h.setBlock(pos.below(), Blocks.AIR);
        }
        h.runAfterDelay(3, () -> {
            for (var entry : FamiliarHomes.ALL) {
                var items = h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(h.absolutePos(new BlockPos(0,0,0)), h.absolutePos(new BlockPos(9,5,9))), e -> e.getItem().is(entry.item().get()));
                check(items.size() == 1 && items.get(0).getItem().getCount() == 1 && !items.get(0).getItem().hasTag(), "support loss did not drop one empty " + entry.kind());
            }
            h.succeed();
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_home_assignment", timeoutTicks = 100)
    public static void bookSelectionRevalidatesOwnerCompatibilityOccupancyAndReplacement(GameTestHelper h) {
        FamiliarFixtures.bound(h, "cat", "home_assignment_" + UUID.randomUUID(), b -> {
            var owner = b.owner(); owner.getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get()));
            var pos = new BlockPos(2,1,3); var bed = home(h, FamiliarHomeKind.BED, pos, owner);
            owner.getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get()));
            check(PlayerFixtures.clickWith(h, owner, new ItemStack(GrimoireItems.BOOK.get()), pos).consumesAction(), "book use failed");
            check(owner.containerMenu instanceof FamiliarRosterMenu menu && menu.homeContext() && menu.home(owner) == bed, "home selection session missing");
            check(FamiliarHomeService.assign(owner, bed, b.bond()).equals("home_assigned") && b.bond().equals(bed.occupant()), "assignment failed");
            var store = FamiliarStore.get(owner.server); var otherState = new CompoundTag(); otherState.putString("command", "stay");
            b.mob().moveTo(Vec3.atBottomCenterOf(bed.getBlockPos().east()));
            long viewRevision = store.roster().revision(owner.getUUID());
            FamiliarHomeRest.tick(new net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent(b.mob()));
            check(b.record().state().getInt("homeRest") == 1 && store.roster().revision(owner.getUUID()) == viewRevision, "internal rest clock invalidated the owner session");
            var other = new FamiliarRecord(UUID.randomUUID(), owner.getUUID(), UUID.randomUUID(), "cat", "minecraft:cat", 1, FamiliarRecord.Phase.ACTIVE, UUID.randomUUID(), 1, otherState, new CompoundTag());
            check(store.create(other) && FamiliarHomeService.assign(owner, bed, other.bond()).equals("home_occupied"), "unloaded bond overwrote occupant");
            var stranger = AnimalFixtures.player(h, "m9_home_foreign"); stranger.getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get()));
            check(!FamiliarHomeService.mayUse(stranger, bed, true) && FamiliarHomeService.assign(stranger, bed, b.bond()).equals("permission"), "foreign assignment");
            var nest = home(h, FamiliarHomeKind.NEST, new BlockPos(6,1,3), owner); owner.getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get()));
            check(FamiliarHomeService.assign(owner, nest, b.bond()).equals("home_incompatible"), "cat accepted a nest");
            var saved = bed.saveWithFullMetadata(); saved.getCompound("Home").putUUID("occupant", UUID.randomUUID()); bed.load(saved); bed.repair();
            check(b.bond().equals(bed.occupant()), "cache acquired authority");
            UUID oldInstance = bed.instance(); var oldHome = b.record().state().getCompound("home");
            h.setBlock(pos, Blocks.AIR); check(!b.record().state().contains("home"), "observed break retained assignment");
            var replacement = home(h, FamiliarHomeKind.BED, pos, owner); owner.getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get()));
            check(!oldInstance.equals(replacement.instance()) && replacement.occupant() == null, "replacement inherited identity");
            store.update(b.record().change(s -> s.put("home", oldHome)), true);
            check(FamiliarHomeTarget.resolve(b.mob(), b.record()).reason().equals("home_missing") && !b.record().state().contains("home"), "stale home identity accepted");
            check(!owner.containerMenu.stillValid(owner), "replaced home retained a valid selection session");
            owner.containerMenu = owner.inventoryMenu; h.succeed();
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_home_return", timeoutTicks = 260)
    public static void returnHomeNavigatesThenSafeOfflineRestHealsWithoutEnergy(GameTestHelper h) {
        FamiliarFixtures.bound(h, "wolf", "home_return_" + UUID.randomUUID(), b -> {
            var owner = b.owner(); var mob = b.mob();
            var bed = home(h, FamiliarHomeKind.BED, new BlockPos(2,1,3), owner); owner.getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get()));
            check(FamiliarHomeService.assign(owner, bed, b.bond()).equals("home_assigned"), "home assignment");
            returning = OnlinePlayers.register(List.of(owner)); mob.moveTo(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(7,1,7))));
            check(FamiliarCommands.set(owner, b.bond(), FamiliarCommand.RETURN_HOME).equals("command_set"), "return command");
            h.runAfterDelay(90, () -> {
                check(b.record().command() == FamiliarCommand.STAY && mob.position().distanceToSqr(Vec3.atBottomCenterOf(bed.getBlockPos().above())) <= 4, "return failed: " + b.record().state());
                returning.close(); returning = null; mob.setHealth(mob.getMaxHealth() - 2); float health = mob.getHealth();
                FamiliarStore.get(owner.server).update(b.record().change(s -> s.putInt("homeRest", 0)), false);
                h.runAfterDelay(100, () -> {
                    check(mob.getHealth() == health + 1, "offline rest did not heal exactly one health");
                    check(WitchStatusService.energyUnits(owner) == 7500 && b.record().state().getInt("reserve") == 0, "rest changed energy or reserve"); h.succeed();
                });
            });
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_home_unloaded", timeoutTicks = 100)
    public static void unloadedHomeIsPendingAndUnknownBlockDataIsReadOnly(GameTestHelper h) {
        FamiliarFixtures.bound(h, "cat", "home_unloaded_" + UUID.randomUUID(), b -> {
            var ref = new CompoundTag(); var pos = new BlockPos(20_000_000, 80, 20_000_000);
            check(!h.getLevel().hasChunkAt(pos), "unloaded fixture already loaded"); ref.putString("dimension", h.getLevel().dimension().location().toString()); ref.putLong("position", pos.asLong()); ref.putUUID("instance", UUID.randomUUID());
            var store = FamiliarStore.get(b.owner().server); check(store.update(b.record().change(s -> s.put("home", ref)), true), "home fixture");
            check(FamiliarHomeTarget.resolve(b.mob(), b.record()).reason().equals("home_unloaded") && b.record().state().contains("home") && !h.getLevel().hasChunkAt(pos), "home loaded terrain or proved absence");
            var home = home(h, FamiliarHomeKind.BED, new BlockPos(2,1,3), b.owner()); b.owner().getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get()));
            check(FamiliarHomeService.assign(b.owner(), home, b.bond()).equals("home_assigned"), "unknown-schema home assignment fixture");
            var assigned = b.record().state().getCompound("home"); var saved = home.saveWithFullMetadata(); saved.getCompound("Home").putInt("schema", 99); home.load(saved);
            check(!home.writable() && home.saveWithFullMetadata().getCompound("Home").getInt("schema") == 99, "unknown home data overwritten");
            check(FamiliarHomeTarget.resolve(b.mob(), b.record()).position() == null && b.record().state().getCompound("home").equals(assigned), "unknown schema was treated as proof of a missing home"); h.succeed();
        });
    }
}
