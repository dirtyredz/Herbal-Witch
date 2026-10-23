package io.github.dirtyredz.herbalwitch.apothecary.shop.showcase;

import io.github.dirtyredz.herbalwitch.apothecary.shop.customer.ShopCustomerRules;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopDisplayBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopRegisterBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link.ShopFixtureHooks;
import io.github.dirtyredz.herbalwitch.apothecary.shop.guide.ShopShowcaseSection;
import io.github.dirtyredz.herbalwitch.apothecary.shop.registry.ShopBlocks;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlan;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;

import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.ORIGIN;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * The commerce showcase inside the built witchcraft showcase: every piece in its documented column,
 * open and closed registers that belong to no shop, demonstration displays with fixed stocked and
 * sold-out projections that refuse links and sell nothing, and one inert villager outside villager demand.
 */
public final class ShopShowcaseChecks {
    private ShopShowcaseChecks() {
    }

    public static void verify(GameTestHelper helper, ShowcasePlan plan) {
        var area = plan.areas().stream().filter(a -> a.id().equals("shop")).findFirst().orElseThrow();
        var level = helper.getLevel();
        for (int i = 0; i < ShopShowcaseSection.EXHIBITS.size(); i++) {
            String exhibit = ShopShowcaseSection.EXHIBITS.get(i);
            BlockPos pos = helper.absolutePos(ORIGIN.offset(area.x() + ShopShowcaseSection.COLUMN, 2, area.z() + ShowcasePlan.HEADING_ROWS + ShopShowcaseSection.row(i)));
            var state = level.getBlockState(pos);
            var be = level.getBlockEntity(pos);
            check(be instanceof ShopFixtureBlockEntity fixture && fixture.shop() == null, exhibit + " belongs to a shop or is missing");
            if (exhibit.startsWith("register")) {
                check(state.is(ShopBlocks.REGISTER.get()) && state.getValue(ShopRegisterBlock.OPEN) == exhibit.endsWith("open"), exhibit + " state");
            } else if (exhibit.equals("board")) {
                check(state.is(ShopBlocks.ORDER_BOARD.get()), "order board missing");
            } else {
                check(be instanceof ShopDisplayBlockEntity, exhibit + " is not a display");
                ShopDisplayBlockEntity display = (ShopDisplayBlockEntity) be;
                boolean stocked = exhibit.endsWith("stocked");
                check(display.demonstration() && display.samples().size() == (exhibit.startsWith("shelf") ? 3 : 1), exhibit + " projection");
                check(display.samples().stream().allMatch(s -> s.available() == stocked), exhibit + " availability");
                check(ShopFixtureHooks.linkedShop(level, pos, display).isEmpty(), exhibit + " sells for a shop");
            }
        }
        AABB box = new AABB(helper.absolutePos(ORIGIN.offset(area.x(), 0, area.z()))).expandTowards(area.width(), 4, area.depth() + ShowcasePlan.HEADING_ROWS);
        var villagers = level.getEntitiesOfClass(Villager.class, box);
        check(villagers.size() == 1, "commerce showcase villager count " + villagers.size());
        Villager villager = villagers.get(0);
        check(villager.isNoAi() && villager.isInvulnerable() && !ShopCustomerRules.eligible(villager), "the demonstration villager can become a customer");
    }
}
