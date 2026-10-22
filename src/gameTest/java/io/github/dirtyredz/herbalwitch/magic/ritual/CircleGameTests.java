package io.github.dirtyredz.herbalwitch.magic.ritual;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.garden.HerbBedBlock;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.GardenBlessingAction;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.HomeProtectionAction;
import io.github.dirtyredz.herbalwitch.magic.ritual.circle.CircleLayout;
import io.github.dirtyredz.herbalwitch.magic.ritual.circle.CircleValidation;
import io.github.dirtyredz.herbalwitch.magic.ritual.circle.RitualCircleBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSession;
import io.github.dirtyredz.herbalwitch.magic.ward.registry.WardRegistration;
import io.github.dirtyredz.herbalwitch.magic.ward.service.WardService;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

import static io.github.dirtyredz.herbalwitch.fixture.BotanyFixtures.bed;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.carry;
import static io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.CIRCLE;
import static io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.circle;
import static io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.energy;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.ALTAR;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.FRONT;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.advance;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.altar;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.open;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.session;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.witch;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * The ritual circle around an altar and its two rituals, run synchronously: validation names missing
 * cells, a circle ritual refuses without a complete circle, the garden blessing enriches the beds
 * around the chosen one and pays once, a broken circle cancels and consumes nothing, and home
 * protection charges a ward stone the witch may bind.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CircleGameTests {
    /** A herb bed outside the circle, within reach of the altar; the blessing's square reaches its neighbours. */
    private static final BlockPos BED = new BlockPos(1, 1, 7);

    private CircleGameTests() {
    }

    private static int count(ServerPlayer p, Item item) {
        return p.getInventory().countItem(item);
    }

    private static void blessingOffering(ServerPlayer p) {
        carry(p, new ItemStack(PreparationItems.powder(HerbCatalog.NETTLE)), new ItemStack(Items.BONE_MEAL),
                new ItemStack(PreparationItems.product(EverydayProductCatalog.SOIL_TONIC)));
    }

    /** One ring cell of the circle (absolute). */
    private static BlockPos gap(GameTestHelper helper) {
        return CircleLayout.cells(helper.absolutePos(CIRCLE), Direction.NORTH).get(0);
    }

    private static boolean enriched(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockState(pos).getValue(HerbBedBlock.ENRICHED);
    }

    @GameTest(template = TEMPLATE)
    public static void aCircleValidatesWholeAndNamesAMissingCell(GameTestHelper helper) {
        altar(helper);
        check(!CircleValidation.around(helper.getLevel(), helper.absolutePos(ALTAR)).complete(), "an altar alone counts as a circle");
        circle(helper);
        check(CircleValidation.around(helper.getLevel(), helper.absolutePos(ALTAR)).complete(), "a placed circle is not complete");
        check(helper.getLevel().getBlockState(gap(helper)).getBlock() instanceof RitualCircleBlock, "the circle's first cell is missing");
        helper.getLevel().setBlock(gap(helper), Blocks.AIR.defaultBlockState(), 2);
        check(!CircleValidation.around(helper.getLevel(), helper.absolutePos(ALTAR)).missing().isEmpty(), "a gap went unnoticed");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void circleRitualsRefuseWithoutACompleteCircle(GameTestHelper helper) {
        altar(helper);
        bed(helper, BED, true);
        ServerPlayer w = witch(helper, "m7_circle_hasty", FRONT);
        blessingOffering(w);
        AltarMenu menu = open(helper, w);
        check(GardenBlessingAction.begin(w, menu, Optional.of(helper.absolutePos(BED))).equals("circle.incomplete"),
                "a blessing began without a circle");
        check(count(w, Items.BONE_MEAL) == 1, "a refused ritual consumed something");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theGardenBlessingEnrichesTheBedsAroundTheChosenOneOnce(GameTestHelper helper) {
        circle(helper);
        bed(helper, BED, true);
        bed(helper, BED.east(), true);
        bed(helper, BED.south(), true); // north of the bed is a circle cell
        ServerPlayer w = witch(helper, "m7_circle_gardener", FRONT);
        blessingOffering(w);
        int before = energy(w);
        AltarMenu menu = open(helper, w);
        String begun = GardenBlessingAction.begin(w, menu, Optional.of(helper.absolutePos(BED)));
        check(begun.equals("garden_blessing.begun"), "did not begin: " + begun);
        AltarSession s = session(w);
        advance(s, WitchRules.server().workings().gardenBlessingTicks() - 1);
        check(s.running() && !enriched(helper, BED), "blessed early");
        advance(s, 1);
        check(s.state() == AltarSession.State.COMPLETED, "did not complete: " + s.state());
        check(enriched(helper, BED) && enriched(helper, BED.east()) && enriched(helper, BED.south()), "a bed in the square was missed");
        check(energy(w) == before - EnergyService.units(WitchRules.server().workings().gardenBlessingCost()), "paid "
                + (before - energy(w)));
        check(count(w, Items.BONE_MEAL) == 0 && count(w, PreparationItems.powder(HerbCatalog.NETTLE)) == 0
                && count(w, Items.GLASS_BOTTLE) == 1, "the offering was not consumed with its bottle back");
        blessingOffering(w);
        check(!GardenBlessingAction.begin(w, open(helper, w), Optional.of(helper.absolutePos(BED))).equals("garden_blessing.begun"),
                "enrichment stacked");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aBrokenCircleCancelsTheRitualAndConsumesNothing(GameTestHelper helper) {
        circle(helper);
        bed(helper, BED, true);
        ServerPlayer w = witch(helper, "m7_circle_clumsy", FRONT);
        blessingOffering(w);
        int before = energy(w);
        String begun = GardenBlessingAction.begin(w, open(helper, w), Optional.of(helper.absolutePos(BED)));
        check(begun.equals("garden_blessing.begun"), "did not begin: " + begun);
        AltarSession s = session(w);
        advance(s, 10);
        helper.getLevel().setBlock(gap(helper), Blocks.AIR.defaultBlockState(), 2);
        advance(s, WitchRules.server().workings().gardenBlessingTicks());
        check(s.state() == AltarSession.State.CANCELLED, "a broken circle carried on: " + s.state());
        check(!enriched(helper, BED) && energy(w) == before && count(w, Items.BONE_MEAL) == 1, "a cancelled blessing consumed something");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_ward_ritual")
    public static void homeProtectionChargesAStoneTheWitchMayBind(GameTestHelper helper) {
        circle(helper);
        BlockPos stone = new BlockPos(1, 1, 7);
        helper.setBlock(stone, WardRegistration.STONE.get().defaultBlockState());
        BlockPos abs = helper.absolutePos(stone);
        ServerPlayer w = witch(helper, "m7_circle_warden", FRONT);
        carry(w, new ItemStack(PreparationItems.powder(HerbCatalog.SAGE)), new ItemStack(Items.AMETHYST_SHARD));
        int before = energy(w);
        String begun = HomeProtectionAction.begin(w, open(helper, w), Optional.of(abs));
        check(begun.equals("home_protection.begun"), "did not begin: " + begun);
        AltarSession s = session(w);
        advance(s, WitchRules.server().workings().homeProtectionTicks());
        check(s.state() == AltarSession.State.COMPLETED, "did not complete: " + s.state());
        check(WardService.record(helper.getLevel(), abs).map(r -> r.charged() && r.ownedBy(w.getUUID())).orElse(false),
                "the stone was not charged and bound");
        check(energy(w) == before - EnergyService.units(WitchRules.server().workings().homeProtectionCost()), "paid " + (before - energy(w)));
        check(count(w, Items.AMETHYST_SHARD) == 0, "the offering was not consumed");
        check(WardService.deactivate(w, helper.getLevel(), abs).equals("deactivated"), "cleanup");
        helper.succeed();
    }
}
