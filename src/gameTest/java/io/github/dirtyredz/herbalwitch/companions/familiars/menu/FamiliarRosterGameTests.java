package io.github.dirtyredz.herbalwitch.companions.familiars.menu;

import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures;
import io.github.dirtyredz.herbalwitch.companions.familiars.network.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarMenus;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class FamiliarRosterGameTests {
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_roster_private", timeoutTicks = 120)
    public static void privatePagesRefreshStaleSessionsAndNearbyDetailCloses(GameTestHelper h) {
        // ANU retains its world between jobs; prior bonds are valid roster entries.
        FamiliarFixtures.bound(h, "cat", "roster_private_" + UUID.randomUUID(), bound -> {
            var owner = bound.owner(); var stranger = AnimalFixtures.player(h, "m9_roster_stranger");
            owner.getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get()));
            stranger.getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get()));
            var one = new FamiliarRosterMenu(FamiliarMenus.ROSTER.get(), 71, 101, null);
            var two = new FamiliarRosterMenu(FamiliarMenus.ROSTER.get(), 72, 102, null);
            owner.containerMenu = one; stranger.containerMenu = two;
            Map<UUID, FamiliarRosterPage> pages = new HashMap<>(); FamiliarPackets.observe((p, page) -> pages.put(p.getUUID(), page));
            try {
                one.send(owner, true, ""); two.send(stranger, true, "");
                check(pages.get(owner.getUUID()).rows().size() == 1 && pages.get(stranger.getUUID()).rows().isEmpty(), "roster leaked a foreign bond");
                var revision = one.sentRevision();
                FamiliarRosterMenu.request(owner, new FamiliarRequest(71, 999, revision, "page", null, bound.bond(), ""));
                check(one.page(owner, "").rows().size() == 1, "forged nonce changed page");
                FamiliarRosterMenu.request(stranger, new FamiliarRequest(72, 102, two.sentRevision(), "detail", bound.bond(), null, ""));
                check(stranger.containerMenu == two && !FamiliarMenuAccess.detail(stranger, bound.record()), "foreign detail opened");
                var store = FamiliarStore.get(owner.server); check(store.update(bound.record().change(s -> s.putString("name", "Updated")), false), "revision setup");
                FamiliarRosterMenu.request(owner, new FamiliarRequest(71, 101, revision, "page", null, bound.bond(), ""));
                check(pages.get(owner.getUUID()).notice().equals("stale") && one.page(owner, "").rows().size() == 1, "stale request applied instead of refreshing");
                var detail = new FamiliarDetailMenu(73, owner.getInventory(), 103, bound.bond());
                check(detail.stillValid(owner), "nearby owner detail refused");
                bound.mob().moveTo(bound.mob().position().add(0, 0, 20)); check(!detail.stillValid(owner), "remote detail retained access");
                check(one.stillValid(owner), "remote familiar incorrectly closed roster");
                owner.getInventory().setItem(0, ItemStack.EMPTY); check(!one.stillValid(owner), "lost book retained roster");
            } finally { FamiliarPackets.observe(null); owner.containerMenu = owner.inventoryMenu; stranger.containerMenu = stranger.inventoryMenu; }
            h.succeed();
        });
    }
}
