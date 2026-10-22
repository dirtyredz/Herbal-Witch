package io.github.dirtyredz.herbalwitch.companions.animals;

import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.animals.inspection.menu.AnimalMenu;
import io.github.dirtyredz.herbalwitch.companions.animals.inspection.network.*;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalCapability;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class AnimalInspectionGameTests {
    @GameTest(template = "empty", batch = "herbalwitch_animal_private_menu")
    public static void privateCardAndSessionValidationPreventReplayAndCloseWhenBookIsLost(GameTestHelper h) {
        floor(h); var first = AnimalFixtures.player(h, "m8_card_a"); var second = AnimalFixtures.player(h, "m8_card_b");
        var cow = AnimalFixtures.animal(h, EntityType.COW); AnimalFixtures.relationship(cow, first.getUUID(), 80, 4);
        first.getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get())); first.getInventory().setItem(1, new ItemStack(AnimalItems.bait(Bait.GRAIN), 2));
        second.getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get()));
        var one = new AnimalMenu(31, first.getInventory(), cow.getUUID(), 101); first.containerMenu = one;
        var two = new AnimalMenu(32, second.getInventory(), cow.getUUID(), 102); second.containerMenu = two;
        Map<UUID, AnimalSnapshot> snapshots = new HashMap<>(); AnimalPackets.observe((p, s) -> snapshots.put(p.getUUID(), s));
        try {
            one.send(first, true); two.send(second, true);
            check(snapshots.size() == 2 && snapshots.get(first.getUUID()).trust() == 80 && snapshots.get(second.getUUID()).trust() == 0, "private relation leaked");
            long revision = one.revision();
            AnimalMenu.request(first, new AnimalRequest(31, 999, cow.getUUID(), revision));
            AnimalMenu.request(first, new AnimalRequest(31, 101, cow.getUUID(), revision + 1));
            check(AnimalCapability.of(cow).orElseThrow().state().orElseThrow().keeper().isEmpty(), "invalid session adopted");
            AnimalMenu.request(first, new AnimalRequest(31, 101, cow.getUUID(), revision));
            check(AnimalCapability.of(cow).orElseThrow().state().orElseThrow().keeper().orElseThrow().equals(first.getUUID()), "valid session refused");
            AnimalMenu.request(first, new AnimalRequest(31, 101, cow.getUUID(), revision));
            check(first.getInventory().getItem(1).getCount() == 1, "replayed request consumed more bait");
            first.getInventory().setItem(0, ItemStack.EMPTY); check(!one.stillValid(first), "lost book retained access");
            check(!two.stillValid(second), "new foreign ownership retained access");
        } finally { AnimalPackets.observe(null); first.containerMenu = first.inventoryMenu; second.containerMenu = second.inventoryMenu; }
        h.succeed();
    }
}
