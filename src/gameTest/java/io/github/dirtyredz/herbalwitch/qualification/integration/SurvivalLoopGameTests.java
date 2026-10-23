package io.github.dirtyredz.herbalwitch.qualification.integration;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.AdvancedProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.MixtureCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.ShopFixtures;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopResult;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopTrades;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.bottling_bench.BottlingBenchBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.cauldron.CauldronBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.drying_rack.DryingRackBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.kettle.KettleBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.mortar.MortarBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.authorization.StationOperations.Operation;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlocks;
import io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures;
import io.github.dirtyredz.herbalwitch.fixture.WitchFixtures;
import io.github.dirtyredz.herbalwitch.magic.witch.status.EnergyService;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

import static io.github.dirtyredz.herbalwitch.fixture.BotanyFixtures.plantOnFarmland;
import static io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.energy;
import static io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.operate;
import static io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.start;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.click;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.campfire;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.inventory;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.place;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.tick;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.water;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/** Milestone 12 cross-milestone integration: the design's gameplay loop end to end through production entry points. */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SurvivalLoopGameTests {
    private static final String TEMPLATE = "empty";
    private static final BlockPos AT = new BlockPos(4, 2, 4);
    private static final BlockPos NEAR = new BlockPos(4, 1, 2);
    private static final BlockPos COUNTER = new BlockPos(4, 1, 6);
    private static final BlockPos BENCH = new BlockPos(6, 1, 6);

    private SurvivalLoopGameTests() {
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_qualification_forage_to_shop_to_non_witch_use", timeoutTicks = 400)
    public static void forageToShopToNonWitchUse(GameTestHelper helper) {
        floor(helper);
        ServerPlayer forager = PlayerFixtures.player(helper, "m12_loop_forager", GameType.SURVIVAL, Direction.NORTH);

        // Forage: harvest both mature plants by hand and keep exactly the herbs the world dropped.
        ItemStack chamomile = harvest(helper, forager, HerbCatalog.CHAMOMILE, new BlockPos(2, 1, 2));
        ItemStack yarrow = harvest(helper, forager, HerbCatalog.YARROW, new BlockPos(6, 1, 2));

        // Dry: one fresh herb per bundle, 1200 ticks each.
        helper.setBlock(AT.below(), Blocks.STONE);
        place(helper, StationBlocks.DRYING_RACK.get(), AT);
        ProcessInventory rack = inventory(helper, AT);
        ItemStack chamomileBundle = run(helper, rack, DryingRackBlockEntity.INPUT, chamomile.split(1),
                DryingRackBlockEntity.OUTPUT, 1_300, AT);
        ItemStack yarrowBundle = run(helper, rack, DryingRackBlockEntity.INPUT, yarrow.split(1),
                DryingRackBlockEntity.OUTPUT, 1_300, AT);
        check(chamomileBundle.is(PreparationItems.bundle(HerbCatalog.CHAMOMILE)), "chamomile dried into " + chamomileBundle);
        check(yarrowBundle.is(PreparationItems.bundle(HerbCatalog.YARROW)), "yarrow dried into " + yarrowBundle);

        // Grind: each bundle becomes its powder in the mortar.
        BlockPos mortarAt = new BlockPos(2, 1, 4);
        place(helper, StationBlocks.MORTAR.get(), mortarAt);
        ProcessInventory mortar = inventory(helper, mortarAt);
        ItemStack chamomilePowder = run(helper, mortar, MortarBlockEntity.INPUT_0, chamomileBundle,
                MortarBlockEntity.OUTPUT, 150, mortarAt);
        ItemStack yarrowPowder = run(helper, mortar, MortarBlockEntity.INPUT_0, yarrowBundle,
                MortarBlockEntity.OUTPUT, 150, mortarAt);
        check(chamomilePowder.is(PreparationItems.powder(HerbCatalog.CHAMOMILE)), "chamomile ground into " + chamomilePowder);
        check(yarrowPowder.is(PreparationItems.powder(HerbCatalog.YARROW)), "yarrow ground into " + yarrowPowder);

        // Steep: both powders and a water bottle in the kettle over a lit campfire.
        BlockPos kettleAt = new BlockPos(6, 2, 4);
        campfire(helper, kettleAt.below(), true);
        place(helper, StationBlocks.KETTLE.get(), kettleAt);
        ProcessInventory kettle = inventory(helper, kettleAt);
        kettle.setStackInSlot(KettleBlockEntity.INPUT_0, chamomilePowder);
        kettle.setStackInSlot(KettleBlockEntity.INPUT_1, yarrowPowder);
        kettle.setStackInSlot(KettleBlockEntity.WATER, water());
        ItemStack tea = waitFor(helper, kettle, KettleBlockEntity.OUTPUT, 300, kettleAt);
        check(tea.is(PreparationItems.product(EverydayProductCatalog.RECOVERY_TEA)), "steeped " + tea);

        // Sell: a witch stocks the tea and opens the shop; she is not online while it sells.
        ShopFixtures.Shop shop = ShopFixtures.shop(helper, ShopFixtures.witch(helper, "m12_loop_owner"), COUNTER);
        ShopFixtures.stock(helper, shop, tea);
        UUID offer = ShopFixtures.offer(helper, shop, 0, 1, 1);
        ShopFixtures.open(helper, shop);
        check(helper.getLevel().getServer().getPlayerList().getPlayer(shop.owner().getUUID()) == null,
                "the owner is online; the shop must sell while she is away");

        // Buy: a non-witch pays one emerald through the production purchase path.
        ServerPlayer buyer = ShopFixtures.player(helper, "m12_loop_buyer");
        check(!WitchStatusService.isWitch(buyer), "the buyer must not be a witch");
        ShopFixtures.emeralds(buyer, 1);
        long revision = ShopFixtures.record(helper, shop).offer(offer).orElseThrow().revision();
        ShopResult sale = ShopTrades.purchase(buyer, ShopFixtures.store(helper), shop.id(), offer, revision, null);
        check(sale.success(), "purchase refused: " + sale.code());
        Item teaItem = PreparationItems.product(EverydayProductCatalog.RECOVERY_TEA);
        check(ShopFixtures.count(buyer, teaItem) == 1, "the buyer did not receive the tea");
        check(ShopFixtures.count(buyer, Items.EMERALD) == 0, "the emerald was not paid");
        check(ShopFixtures.record(helper, shop).proceeds == 1, "proceeds " + ShopFixtures.record(helper, shop).proceeds);

        // Use: the non-witch drinks it.
        ItemStack left = drink(helper, buyer, takeFromInventory(buyer, teaItem));
        MobEffectInstance regeneration = buyer.getEffect(MobEffects.REGENERATION);
        check(regeneration != null && regeneration.getDuration() == 90 * 20, "recovery tea gave " + regeneration);
        check(left.is(Items.GLASS_BOTTLE), "the bottle did not come back: " + left);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_qualification_magical_brew_requires_a_witch_and_charges_once",
            timeoutTicks = 400)
    public static void magicalBrewRequiresAWitchAndChargesOnce(GameTestHelper helper) {
        floor(helper);
        campfire(helper, AT.below(), true);
        place(helper, StationBlocks.CAULDRON.get(), AT);
        ProcessInventory cauldron = inventory(helper, AT);
        cauldron.setStackInSlot(CauldronBlockEntity.INPUT_0, new ItemStack(PreparationItems.powder(HerbCatalog.MUGWORT)));
        cauldron.setStackInSlot(CauldronBlockEntity.INPUT_1, new ItemStack(Items.CARROT));
        cauldron.setStackInSlot(CauldronBlockEntity.BASE, water());

        // A non-witch is refused and nothing is reserved.
        ServerPlayer novice = WitchFixtures.player(helper, "m12_loop_novice", NEAR);
        String refusal = operate(helper, novice, AT, Operation.START);
        check(refusal.equals("not_witch"), "a non-witch start gave " + refusal);
        check(cauldron.reserved(CauldronBlockEntity.INPUT_0) == 0 && cauldron.reserved(CauldronBlockEntity.INPUT_1) == 0,
                "inputs reserved");
        check(cauldron.getStackInSlot(CauldronBlockEntity.INPUT_0).getCount() == 1
                        && cauldron.getStackInSlot(CauldronBlockEntity.INPUT_1).getCount() == 1
                        && cauldron.getStackInSlot(CauldronBlockEntity.BASE).getCount() == 1,
                "inventory changed by the refusal");
        tick(helper, AT, 50);
        check(cauldron.getStackInSlot(CauldronBlockEntity.OUTPUT).isEmpty(), "brewed without a witch");

        // A witch starts it; the mixture appears and the recipe's energy (5) is spent exactly once.
        ServerPlayer witch = WitchFixtures.witch(helper, "m12_loop_brewer", NEAR);
        int before = energy(witch);
        start(helper, witch, AT);
        ItemStack mixture = waitFor(helper, cauldron, CauldronBlockEntity.OUTPUT, 700, AT);
        check(mixture.is(AdvancedPreparations.item(MixtureCatalog.of(AdvancedProductCatalog.NIGHT_VISION).orElseThrow().spec())),
                "made " + mixture);
        check(energy(witch) == before - EnergyService.units(5), "charged " + (before - energy(witch)) + " units");
        tick(helper, AT, 1_300);
        check(energy(witch) == before - EnergyService.units(5), "charged again without a new start");

        // Bottle the mixture at the bench, then a non-witch drinks one dose.
        place(helper, StationBlocks.BOTTLING_BENCH.get(), BENCH);
        ProcessInventory bench = inventory(helper, BENCH);
        bench.setStackInSlot(BottlingBenchBlockEntity.INPUT_0, mixture);
        bench.setStackInSlot(BottlingBenchBlockEntity.PACKAGING, new ItemStack(Items.GLASS_BOTTLE, MixtureCatalog.DOSES));
        ItemStack draughts = waitFor(helper, bench, BottlingBenchBlockEntity.OUTPUT, 200, BENCH);
        check(draughts.is(AdvancedPreparations.item(AdvancedProductCatalog.NIGHT_VISION)), "bottled " + draughts);
        ItemStack left = drink(helper, novice, draughts.split(1));
        check(novice.hasEffect(MobEffects.NIGHT_VISION), "no night vision");
        check(left.is(Items.GLASS_BOTTLE), "the bottle did not come back: " + left);
        helper.succeed();
    }

    /** Harvests the mature plant on new farmland and returns every fresh herb it dropped, as one stack. */
    private static ItemStack harvest(GameTestHelper helper, ServerPlayer player, HerbSpec herb, BlockPos soil) {
        BlockPos plant = plantOnFarmland(helper, herb, soil, HerbSpec.MAX_AGE);
        check(click(helper, player, plant).consumesAction(), herb.id() + " was not harvested");
        ItemStack fresh = collectDropped(helper, BotanyItems.fresh(herb));
        check(fresh.getCount() >= 2, herb.id() + " gave " + fresh);
        return fresh;
    }

    /** Removes the loose {@code item} entities in the test area and returns their merged stack. */
    private static ItemStack collectDropped(GameTestHelper helper, Item item) {
        AABB area = new AABB(helper.absolutePos(BlockPos.ZERO), helper.absolutePos(new BlockPos(9, 6, 9))).inflate(1);
        ItemStack total = new ItemStack(item, 0);
        for (ItemEntity drop : helper.getLevel().getEntitiesOfClass(ItemEntity.class, area, e -> e.getItem().is(item))) {
            total.grow(drop.getItem().getCount());
            drop.discard();
        }
        return total;
    }

    /** Puts {@code input} in a station slot, ticks until the output slot fills (bounded) and moves the output out. */
    private static ItemStack run(GameTestHelper helper, ProcessInventory inv, int inputSlot, ItemStack input, int outputSlot,
                                 int maxTicks, BlockPos at) {
        inv.setStackInSlot(inputSlot, input);
        return waitFor(helper, inv, outputSlot, maxTicks, at);
    }

    private static ItemStack waitFor(GameTestHelper helper, ProcessInventory inv, int outputSlot, int maxTicks, BlockPos at) {
        for (int i = 0; i < maxTicks && inv.getStackInSlot(outputSlot).isEmpty(); i++) {
            tick(helper, at, 1);
        }
        check(!inv.getStackInSlot(outputSlot).isEmpty(),
                "nothing appeared in output slot " + outputSlot + " within " + maxTicks + " ticks");
        ItemStack stack = inv.getStackInSlot(outputSlot).copy();
        inv.setStackInSlot(outputSlot, ItemStack.EMPTY);
        return stack;
    }

    private static ItemStack takeFromInventory(ServerPlayer player, Item item) {
        for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
            ItemStack stack = player.getInventory().items.get(slot);
            if (stack.is(item)) {
                player.getInventory().items.set(slot, ItemStack.EMPTY);
                return stack;
            }
        }
        throw new GameTestAssertException("no " + item + " in the inventory");
    }

    /** Finishes using {@code stack} from the player's hand; returns what the hand holds afterwards. */
    private static ItemStack drink(GameTestHelper helper, ServerPlayer player, ItemStack stack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        ItemStack after = stack.getItem().finishUsingItem(stack, helper.getLevel(), player);
        player.setItemInHand(InteractionHand.MAIN_HAND, after);
        return after;
    }
}
