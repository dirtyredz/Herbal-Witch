package io.github.dirtyredz.herbalwitch.companions.familiars.ability;

import io.github.dirtyredz.herbalwitch.companions.familiars.ability.retrieval.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.equipment.FamiliarEquipmentInventory;
import io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.*;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.*;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class FamiliarRetrievalGameTests {
    private static ItemEntity dropped(GameTestHelper h, FamiliarFixtures.Bound b, int count) {
        var pos = b.mob().position().add(1, 0, 0); var item = new ItemEntity(h.getLevel(), pos.x, pos.y, pos.z, new ItemStack(Items.STICK, count));
        item.setThrower(b.owner().getUUID()); item.setNoPickUpDelay(); check(h.getLevel().addFreshEntity(item), "item fixture insertion"); return item;
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_pickup", timeoutTicks = 120)
    public static void wholePickupPaysOnceAndRejectsTheStaleSourceChunk(GameTestHelper h) {
        FamiliarFixtures.bound(h, "raven", "pickup_" + UUID.randomUUID(), b -> h.runAfterDelay(6, () -> {
            try (var online = OnlinePlayers.register(List.of(b.owner()))) {
                var item = dropped(h, b, 8); var source = new CompoundTag(); item.save(source);
                var observed = item.getItem().save(new CompoundTag());
                check(FamiliarRetrieveItem.apply(b.owner(), b.mob(), item, observed), "eligible pickup refused");
                check(item.isRemoved() && FamiliarEquipmentInventory.cargo(b.record()).getCount() == 8, "pickup failed conservation");
                check(WitchStatusService.energyUnits(b.owner()) == 7000, "pickup charged wrong energy");
                check(!FamiliarRetrieveItem.apply(b.owner(), b.mob(), item, observed), "same source paid twice");
                var stale = new ItemEntity(h.getLevel(), 0, 0, 0, ItemStack.EMPTY); stale.load(source);
                check(!h.getLevel().addFreshEntity(stale) && stale.isRemoved(), "stale source chunk recreated retired items");
                check(FamiliarEquipmentInventory.cargo(b.record()).getCount() == 8 && WitchStatusService.energyUnits(b.owner()) == 7000, "duplicate attempt changed cargo/payment"); h.succeed();
            }
        }));
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_pickup_refusal", timeoutTicks = 120)
    public static void changedPayloadPublicDropsAndFullCargoAreRefusedWithoutMutation(GameTestHelper h) {
        FamiliarFixtures.bound(h, "fox", "pickup_refusal_" + UUID.randomUUID(), b -> h.runAfterDelay(6, () -> {
            try (var online = OnlinePlayers.register(List.of(b.owner()))) {
                var item = dropped(h, b, 9); var before = b.record(); int energy = WitchStatusService.energyUnits(b.owner());
                check(!FamiliarRetrieveItem.apply(b.owner(), b.mob(), item, item.getItem().save(new CompoundTag())), "oversize stack picked up");
                item.getItem().setCount(2); var observed = item.getItem().save(new CompoundTag()); item.getItem().setCount(3);
                check(!FamiliarRetrieveItem.apply(b.owner(), b.mob(), item, observed), "changed stack accepted");
                item.setThrower(UUID.randomUUID());
                check(!FamiliarRetrieveItem.apply(b.owner(), b.mob(), item, item.getItem().save(new CompoundTag())), "unrelated public drop accepted");
                item.setThrower(b.owner().getUUID()); item.getItem().getOrCreateTag().putString("payload", "retain");
                check(!FamiliarRetrieveItem.apply(b.owner(), b.mob(), item, item.getItem().save(new CompoundTag())), "payload-bearing item accepted");
                check(!item.isRemoved() && item.getItem().getCount() == 3 && b.record().equals(before) && WitchStatusService.energyUnits(b.owner()) == energy, "refusal mutated authority"); h.succeed();
            }
        }));
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_pickup_permission", timeoutTicks = 120)
    public static void pickupRechecksItsEntityPermissionAndOwnership(GameTestHelper h) {
        FamiliarFixtures.bound(h, "raven", "pickup_permission_" + UUID.randomUUID(), b -> h.runAfterDelay(6, () -> {
            try (var online = OnlinePlayers.register(List.of(b.owner()))) {
                var item = dropped(h, b, 1); boolean[] blocked = {true};
                ItemPickupPermission.register((actor, target) -> blocked[0] && target == item ? EntityActionPermission.Denial.CLAIMS : EntityActionPermission.Denial.NONE);
                var before = b.record(); var observed = item.getItem().save(new CompoundTag());
                check(!FamiliarRetrieveItem.apply(b.owner(), b.mob(), item, observed), "entity pickup veto bypassed"); blocked[0] = false;
                item.setTarget(UUID.randomUUID()); check(!FamiliarRetrieveItem.apply(b.owner(), b.mob(), item, observed), "foreign exclusive item owner bypassed");
                item.setTarget(b.owner().getUUID()); item.setPickUpDelay(20);
                check(!FamiliarRetrieveItem.apply(b.owner(), b.mob(), item, observed), "pickup delay bypassed");
                check(b.record().equals(before) && !item.isRemoved() && WitchStatusService.energyUnits(b.owner()) == 7500, "permission refusal spent"); h.succeed();
            }
        }));
    }
}
