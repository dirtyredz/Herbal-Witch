package io.github.dirtyredz.herbalwitch.preparations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.botany.garden.HerbBedBlock;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.brewing.application.DrinkApplication;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.ProductSpec;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationEffects;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.BotanyFixtures.bed;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.click;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.useItemOn;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.clearDrops;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.dropped;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/** Products doing their ordinary jobs once, server side: drinks, salve, oils and garden products. */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PreparationUseGameTests {
    private PreparationUseGameTests() {
    }

    private static ItemStack product(ProductSpec spec, int count) {
        return new ItemStack(PreparationItems.product(spec), count);
    }

    /** Finishes using one item from the player's hand; returns what the hand holds afterwards. */
    private static ItemStack finish(GameTestHelper helper, ServerPlayer player, ItemStack stack) {
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
        ItemStack after = stack.getItem().finishUsingItem(stack, helper.getLevel(), player);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, after);
        return after;
    }

    @GameTest(template = TEMPLATE)
    public static void drinksApplyTheirEffectAndReturnOneBottle(GameTestHelper helper) {
        ServerPlayer player = player(helper, "drinker", GameType.SURVIVAL, Direction.NORTH);
        ItemStack after = finish(helper, player, product(EverydayProductCatalog.RECOVERY_TEA, 2));
        MobEffectInstance regen = player.getEffect(MobEffects.REGENERATION);
        check(regen != null && regen.getDuration() == 90 * 20 && regen.getAmplifier() == 0, "recovery tea: " + regen);
        check(after.getCount() == 1 && player.getInventory().countItem(Items.GLASS_BOTTLE) == 1, "bottle not returned once");
        finish(helper, player, product(EverydayProductCatalog.RECOVERY_TEA, 1));
        check(player.getEffect(MobEffects.REGENERATION).getAmplifier() == 0, "effects must refresh, not stack");
        ItemStack last = finish(helper, player, product(EverydayProductCatalog.TRAVEL_TONIC, 1));
        check(last.is(Items.GLASS_BOTTLE), "the last drink leaves the bottle in hand");
        check(player.hasEffect(MobEffects.MOVEMENT_SPEED), "travel tonic");
        finish(helper, player, product(EverydayProductCatalog.MINING_TONIC, 1));
        check(player.hasEffect(MobEffects.DIG_SPEED), "mining tonic");
        finish(helper, player, product(EverydayProductCatalog.COOLING_REMEDY, 1));
        check(player.getEffect(MobEffects.FIRE_RESISTANCE).getDuration() == 60 * 20, "cooling remedy");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void energyTeaFeedsAndCoolsDownRestfulResetsRestWarmthStopsFreezing(GameTestHelper helper) {
        ServerPlayer player = player(helper, "tea_time", GameType.SURVIVAL, Direction.NORTH);
        player.getFoodData().setFoodLevel(10);
        finish(helper, player, product(EverydayProductCatalog.ENERGY_TEA, 1));
        check(player.getFoodData().getFoodLevel() == 10 + DrinkApplication.ENERGY_NUTRITION, "food " + player.getFoodData().getFoodLevel());
        check(player.getCooldowns().isOnCooldown(PreparationItems.product(EverydayProductCatalog.ENERGY_TEA)), "no cooldown");
        player.getStats().setValue(player, Stats.CUSTOM.get(Stats.TIME_SINCE_REST), 100_000);
        finish(helper, player, product(EverydayProductCatalog.RESTFUL_INFUSION, 1));
        check(player.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)) == 0, "rest statistic not reset");
        finish(helper, player, product(EverydayProductCatalog.WARMTH_REMEDY, 1));
        check(player.hasEffect(PreparationEffects.WARMTH.get()), "no warmth");
        player.setTicksFrozen(100);
        player.getEffect(PreparationEffects.WARMTH.get()).tick(player, () -> {
        });
        check(player.getTicksFrozen() == 0, "warmth did not clear freezing");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void salveHealsHurtPlayersOnlyAndReturnsTheBowl(GameTestHelper helper) {
        ServerPlayer player = player(helper, "patient", GameType.SURVIVAL, Direction.NORTH);
        player.setHealth(player.getMaxHealth());
        ItemStack salve = product(EverydayProductCatalog.HEALING_SALVE, 1);
        check(!salve.use(helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND).getResult().consumesAction(),
                "salve used at full health");
        player.setHealth(10);
        ItemStack after = finish(helper, player, salve);
        check(player.getHealth() == 14, "healed to " + player.getHealth());
        check(after.is(Items.BOWL), "bowl not returned: " + after);
        check(player.getCooldowns().isOnCooldown(PreparationItems.product(EverydayProductCatalog.HEALING_SALVE)), "no cooldown");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void pestRepellentTurnsOnlyPestsAway(GameTestHelper helper) {
        floor(helper);
        ServerPlayer player = player(helper, "pest_free", GameType.SURVIVAL, Direction.NORTH);
        Silverfish silverfish = helper.spawn(EntityType.SILVERFISH, new BlockPos(2, 1, 2));
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(6, 1, 6));
        silverfish.setTarget(player);
        zombie.setTarget(player);
        finish(helper, player, product(EverydayProductCatalog.PEST_REPELLENT, 1));
        check(player.hasEffect(PreparationEffects.PEST_REPELLENT.get()), "no repellent effect");
        check(silverfish.getTarget() == null, "silverfish still targets the player");
        silverfish.setTarget(player);
        check(silverfish.getTarget() == null, "silverfish re-acquired the player");
        check(zombie.getTarget() == player, "the repellent must not touch other mobs");
        silverfish.discard();
        zombie.discard();
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void gardenProductsChangeOneBlockAndConsumeOnlyOnSuccess(GameTestHelper helper) {
        floor(helper);
        ServerPlayer player = player(helper, "gardener_m3", GameType.SURVIVAL, Direction.NORTH);
        HerbSpec herb = HerbCatalog.CHAMOMILE;
        BlockPos soil = new BlockPos(2, 1, 2);
        bed(helper, soil, true);
        helper.setBlock(soil.above(), BotanyBlocks.plant(herb).getStateForAge(0));
        // Growth infusion: straight to maturity, bottle back.
        ItemStack growth = product(EverydayProductCatalog.GROWTH_INFUSION, 2);
        check(useItemOn(helper, player, growth, soil.above(), Direction.UP).consumesAction(), "growth infusion refused");
        check(helper.getBlockState(soil.above()).getValue(HerbCropBlock.AGE) == 3 && growth.getCount() == 1, "not matured once");
        check(!useItemOn(helper, player, growth, soil.above(), Direction.UP).consumesAction() && growth.getCount() == 1,
                "used on a mature plant");
        // Soil tonic: enrich, then one extra herb on the next harvest only.
        ItemStack tonic = product(EverydayProductCatalog.SOIL_TONIC, 2);
        check(useItemOn(helper, player, tonic, soil, Direction.UP).consumesAction(), "soil tonic refused");
        check(helper.getBlockState(soil).getValue(HerbBedBlock.ENRICHED), "bed not enriched");
        check(!useItemOn(helper, player, tonic, soil, Direction.UP).consumesAction() && tonic.getCount() == 1, "enriched twice");
        clearDrops(helper);
        click(helper, player, soil.above());
        int fresh = dropped(helper, BotanyItems.fresh(herb));
        check(fresh >= 3, "enriched harvest gave " + fresh);
        check(!helper.getBlockState(soil).getValue(HerbBedBlock.ENRICHED), "enrichment not cleared");
        // Fertilizer: only on crops, consumed only on success.
        ItemStack fertilizer = product(EverydayProductCatalog.FERTILIZER, 2);
        check(!useItemOn(helper, player, fertilizer, new BlockPos(6, 0, 6), Direction.UP).consumesAction() && fertilizer.getCount() == 2,
                "fertilizer used on stone");
        check(useItemOn(helper, player, fertilizer, soil.above(), Direction.UP).consumesAction() && fertilizer.getCount() == 1,
                "fertilizer failed on a sprout");
        // Flower dust on grass grows one flower above it.
        BlockPos grass = new BlockPos(6, 1, 2);
        helper.setBlock(grass, Blocks.GRASS_BLOCK);
        ItemStack dust = product(EverydayProductCatalog.FLOWER_DUST, 1);
        check(useItemOn(helper, player, dust, grass, Direction.UP).consumesAction() && dust.isEmpty(), "flower dust failed");
        check(helper.getBlockState(grass.above()).is(net.minecraft.tags.BlockTags.SMALL_FLOWERS), "no flower grew");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void adventurePlayersCannotUseGardenProducts(GameTestHelper helper) {
        floor(helper);
        ServerPlayer visitor = player(helper, "visitor_m3", GameType.ADVENTURE, Direction.NORTH);
        BlockPos grass = new BlockPos(4, 1, 4);
        helper.setBlock(grass, Blocks.GRASS_BLOCK);
        ItemStack dust = product(EverydayProductCatalog.FLOWER_DUST, 1);
        useItemOn(helper, visitor, dust, grass, Direction.UP);
        check(dust.getCount() == 1 && helper.getBlockState(grass.above()).isAir(), "adventure player grew a flower");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void differentWaterDataNeverStacks(GameTestHelper helper) {
        ItemStack plain = product(EverydayProductCatalog.RECOVERY_TEA, 1);
        ItemStack pure = plain.copy();
        pure.getOrCreateTag().putInt("Purity", 3);
        check(!ItemStack.isSameItemSameTags(plain, pure), "teas of different purity would stack");
        helper.succeed();
    }
}
