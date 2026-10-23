package io.github.dirtyredz.herbalwitch.companions.familiars.equipment;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction.FamiliarPlayerReceipt;
import io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures;
import io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarDetailMenu;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarItems;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmCatalog;
import io.github.dirtyredz.herbalwitch.magic.charm.equipment.*;
import io.github.dirtyredz.herbalwitch.magic.charm.item.ProtectiveCharmItem;
import io.github.dirtyredz.herbalwitch.magic.charm.registry.CharmItems;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.gametest.*;
import java.util.UUID;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class FamiliarEquipmentGameTests {
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_cargo_session", timeoutTicks = 100)
    public static void cargoRequiresTheCurrentNearbySessionAndCollectsOnce(GameTestHelper h) {
        FamiliarFixtures.bound(h, "raven", "cargo_session_" + UUID.randomUUID(), b -> {
            var store = FamiliarStore.get(b.owner().server);
            store.update(FamiliarEquipmentInventory.cargo(b.record(), new ItemStack(Items.WHEAT_SEEDS, 8)), true);
            var menu = menu(b); menu.send(b.owner(), true, "");
            var forged = new io.github.dirtyredz.herbalwitch.companions.familiars.network.FamiliarRequest(
                    menu.containerId, menu.nonce() + 1, menu.sentRevision(), "cargo", b.bond(), null, "");
            io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarRosterMenu.request(b.owner(), forged);
            check(FamiliarEquipmentInventory.cargo(b.record()).getCount() == 8, "forged nonce collected cargo");
            h.runAfterDelay(6, () -> {
                check(menu.collectCargo(b.owner()).equals("cargo_collected"), "valid cargo collection failed");
                check(FamiliarEquipmentInventory.cargo(b.record()).isEmpty() && b.owner().getInventory().countItem(Items.WHEAT_SEEDS) == 8, "cargo was not conserved");
                check(menu.collectCargo(b.owner()).equals("cargo_empty") && b.owner().getInventory().countItem(Items.WHEAT_SEEDS) == 8, "cargo replayed");
                check(!FamiliarPlayerReceipt.pending(b.owner()), "cargo receipt remained pending");
                b.owner().closeContainer(); h.succeed();
            });
        });
    }
    private static FamiliarDetailMenu menu(FamiliarFixtures.Bound b) {
        b.owner().getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get()));
        var menu = new FamiliarDetailMenu(40, b.owner().getInventory(), 24L, b.bond()); b.owner().containerMenu = menu;
        check(menu.stillValid(b.owner()), "nearby equipment fixture invalid"); return menu;
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_equipment_clicks", timeoutTicks = 120)
    public static void normalAndShiftClicksConserveGearAndRejectForeignSpecies(GameTestHelper h) {
        FamiliarFixtures.bound(h, "cat", "equipment_clicks_" + UUID.randomUUID(), b -> {
            var menu = menu(b); var owner = b.owner(); var bell = FamiliarItems.gear("cat_bell");
            owner.getInventory().setItem(9, new ItemStack(bell));
            h.runAfterDelay(6, () -> {
                menu.clicked(2, 0, ClickType.QUICK_MOVE, owner);
                check(owner.getInventory().getItem(9).isEmpty() && FamiliarEquipmentInventory.item(b.record(), 1).is(bell), "shift insert lost gear");
                check(!FamiliarPlayerReceipt.pending(owner), "shift insert receipt not completed");
                h.runAfterDelay(6, () -> {
                    menu.clicked(1, 0, ClickType.PICKUP, owner);
                    check(menu.getCarried().is(bell) && FamiliarEquipmentInventory.item(b.record(), 1).isEmpty() && FamiliarPlayerReceipt.pending(owner), "cursor was not durably held");
                    check(b.record().state().getCompound("receipt").contains("cursor"), "cursor missing from receipt");
                    float health = b.mob().getHealth();
                    b.mob().hurt(b.mob().damageSources().generic(), 1000);
                    check(b.mob().isAlive() && b.mob().getHealth() == health, "pending cursor allowed destructive damage");
                    h.runAfterDelay(6, () -> {
                        menu.clicked(2, 0, ClickType.PICKUP, owner);
                        check(menu.getCarried().isEmpty() && owner.getInventory().getItem(9).is(bell) && !FamiliarPlayerReceipt.pending(owner), "normal extraction did not settle");
                        owner.getInventory().setItem(10, new ItemStack(FamiliarItems.gear("wolf_harness")));
                        h.runAfterDelay(6, () -> {
                            long revision = b.record().revision();
                            menu.clicked(3, 0, ClickType.QUICK_MOVE, owner);
                            check(!owner.getInventory().getItem(10).isEmpty() && FamiliarEquipmentInventory.item(b.record(), 1).isEmpty(), "foreign gear entered the slot");
                            check(b.record().revision() == revision, "rejected gear created a transfer receipt");
                            check(WitchStatusService.energyUnits(owner) == 7500, "equipping spent energy");
                            owner.closeContainer(); h.succeed();
                        });
                    });
                });
            });
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_equipment_full", timeoutTicks = 100)
    public static void fullInventoryLeavesCargoAndRetainsPendingCursorWithoutDrops(GameTestHelper h) {
        FamiliarFixtures.bound(h, "cat", "equipment_full_" + UUID.randomUUID(), b -> {
            var owner = b.owner(); var store = FamiliarStore.get(owner.server); var bell = new ItemStack(FamiliarItems.gear("cat_bell"));
            store.update(FamiliarEquipmentInventory.equipment(b.record(), ItemStack.EMPTY, bell), true);
            store.update(FamiliarEquipmentInventory.cargo(b.record(), new ItemStack(Items.WHEAT_SEEDS, 8)), true);
            for (int i = 1; i < 36; i++) owner.getInventory().setItem(i, new ItemStack(Items.COBBLESTONE, 64));
            var menu = menu(b);
            check(menu.collectCargo(owner).equals("inventory_full") && FamiliarEquipmentInventory.cargo(b.record()).getCount() == 8, "full inventory changed cargo");
            h.runAfterDelay(6, () -> {
                menu.clicked(1, 0, ClickType.PICKUP, owner); check(menu.getCarried().is(bell.getItem()), "cursor fixture");
                owner.closeContainer();
                check(b.record().phase() == FamiliarRecord.Phase.QUARANTINED && !b.record().payload().isEmpty() && b.mob().isRemoved(), "full cursor did not suspend with retained recovery");
                check(menu.getCarried().isEmpty() && ItemStack.of(b.record().state().getCompound("receipt").getCompound("cursor")).is(bell.getItem()), "full cursor lost instead of escrowed");
                check(FamiliarEquipmentInventory.cargo(b.record()).getCount() == 8, "unrelated cargo lost");
                check(h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, b.mob().getBoundingBox().inflate(6), item -> item.getItem().is(bell.getItem())).isEmpty(), "full cursor scattered gear"); h.succeed();
            });
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_equipment_charm", timeoutTicks = 100)
    public static void charmReusesOneM7EffectAndPreservesItsPartialSecondAcrossUnequip(GameTestHelper h) {
        FamiliarFixtures.bound(h, "cat", "equipment_charm_" + UUID.randomUUID(), b -> {
            var store = FamiliarStore.get(b.owner().server); var charm = new ItemStack(CharmItems.item(CharmCatalog.PROTECTIVE_SACHET));
            store.update(FamiliarEquipmentInventory.equipment(b.record(), charm, ItemStack.EMPTY), true);
            var event = new LivingEvent.LivingTickEvent(b.mob());
            for (int i = 0; i < 19; i++) FamiliarCharmAdapter.tick(event);
            check(b.record().state().getInt("charmTicks") == 19 && ProtectiveCharmItem.used(FamiliarEquipmentInventory.item(b.record(), 0)) == 0, "partial second charged early");
            var worn = FamiliarEquipmentInventory.item(b.record(), 0);
            store.update(FamiliarEquipmentInventory.equipment(b.record(), ItemStack.EMPTY, ItemStack.EMPTY), true);
            FamiliarCharmAdapter.tick(event); check(b.record().state().getInt("charmTicks") == 19, "unequip reset partial time");
            store.update(FamiliarEquipmentInventory.equipment(b.record(), worn, ItemStack.EMPTY), true);
            FamiliarCharmAdapter.tick(event); check(ProtectiveCharmItem.used(FamiliarEquipmentInventory.item(b.record(), 0)) == 1 && b.record().state().getInt("charmTicks") == 0, "re-equip did not finish partial second");
            b.mob().setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(CharmItems.item(CharmCatalog.UNDEAD_AMULET)));
            var zombie = h.spawn(EntityType.ZOMBIE, new BlockPos(7,1,7)); zombie.setNoAi(true);
            var damage = new LivingDamageEvent(b.mob(), zombie.damageSources().mobAttack(zombie), 10);
            CharmEvents.onDamage(damage); check(damage.getAmount() == 8.5F, "intrinsic/Curios charm replaced or duplicated canonical protection");
            var almost = FamiliarEquipmentInventory.item(b.record(), 0); almost.getOrCreateTagElement(ProtectiveCharmItem.TAG).putInt("Used", ProtectiveCharmItem.capacity() - 1);
            store.update(FamiliarEquipmentInventory.equipment(b.record(), almost, ItemStack.EMPTY), true);
            for (int i = 0; i < 20; i++) FamiliarCharmAdapter.tick(event);
            check(FamiliarEquipmentInventory.item(b.record(), 0).isEmpty() && b.record().state().getInt("charmTicks") == 0 && CharmEquipment.active(b.mob()).isEmpty(), "spent charm did not break or fell back to intrinsic slot");
            h.succeed();
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_equipment_armor", timeoutTicks = 100)
    public static void harnessCannotStackAndDistantDetailCannotExtract(GameTestHelper h) {
        FamiliarFixtures.bound(h, "wolf", "equipment_armor_" + UUID.randomUUID(), b -> {
            var store = FamiliarStore.get(b.owner().server); double before = b.mob().getAttributeValue(Attributes.ARMOR);
            store.update(FamiliarEquipmentInventory.equipment(b.record(), ItemStack.EMPTY, new ItemStack(FamiliarItems.gear("wolf_harness"))), true);
            var tick = new LivingEvent.LivingTickEvent(b.mob()); for (int i = 0; i < 10; i++) FamiliarEquipmentEffects.tick(tick);
            check(b.mob().getAttributeValue(Attributes.ARMOR) == before + 2, "harness modifier stacked");
            var menu = menu(b); b.owner().moveTo(b.owner().position().add(30, 0, 0));
            menu.clicked(1, 0, ClickType.QUICK_MOVE, b.owner());
            check(!menu.stillValid(b.owner()) && !FamiliarEquipmentInventory.item(b.record(), 1).isEmpty(), "distant extraction succeeded");
            store.update(FamiliarEquipmentInventory.equipment(b.record(), ItemStack.EMPTY, ItemStack.EMPTY), true); FamiliarEquipmentEffects.tick(tick);
            check(b.mob().getAttributeValue(Attributes.ARMOR) == before, "unequip retained harness armor"); b.owner().closeContainer(); h.succeed();
        });
    }
}
