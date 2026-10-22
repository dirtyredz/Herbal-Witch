package io.github.dirtyredz.herbalwitch.apothecary.botany;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.botany.conditions.HerbConditionReloadListener;
import io.github.dirtyredz.herbalwitch.apothecary.botany.conditions.HerbConditions;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.github.dirtyredz.herbalwitch.fixture.BotanyFixtures.grid;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * Getting herbs in any world: starter seed recipes, the herb bed recipe, and seeds foraged from
 * grass broken by a player (never by shears, Silk Touch or no player). Conditions load from data.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HerbAcquisitionGameTests {
    private static final int ROLLS = 3000;

    private HerbAcquisitionGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void startersCraftFromWheatSeedsAndBedsFromSlabsAndDirt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Map<HerbSpec, ItemStack> companions = Map.of(HerbCatalog.LAVENDER, new ItemStack(Items.LILAC),
                HerbCatalog.SAGE, new ItemStack(Items.FERN), HerbCatalog.MINT, new ItemStack(Items.SUGAR_CANE),
                HerbCatalog.CHAMOMILE, new ItemStack(Items.DANDELION));
        companions.forEach((herb, companion) -> {
            var g = grid(new ItemStack(Items.WHEAT_SEEDS), companion);
            Optional<CraftingRecipe> recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, g, level);
            check(recipe.isPresent(), herb.id() + ": no starter recipe");
            ItemStack out = recipe.get().assemble(g, level.registryAccess());
            check(out.is(BotanyItems.seeds(herb)) && out.getCount() == 1, herb.id() + " made " + out);
        });
        ItemStack e = ItemStack.EMPTY;
        ItemStack dirt = new ItemStack(Items.DIRT);
        ItemStack slab = new ItemStack(Items.SPRUCE_SLAB);
        var bed = grid(dirt, e, dirt, slab, slab, slab, e, e, e);
        Optional<CraftingRecipe> recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, bed, level);
        check(recipe.isPresent(), "no herb bed recipe");
        ItemStack out = recipe.get().assemble(bed, level.registryAccess());
        check(out.is(BotanyItems.HERB_BED.get()) && out.getCount() == 2, "bed recipe made " + out);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void playersForageStarterSeedsFromGrassOnly(GameTestHelper helper) {
        ServerPlayer player = player(helper, "forager", GameType.SURVIVAL, Direction.NORTH);
        int byHand = herbSeeds(helper, player, ItemStack.EMPTY);
        check(byHand > ROLLS / 32, "only " + byHand + " herb seeds in " + ROLLS + " grass breaks (expected about " + ROLLS / 16 + ")");
        check(byHand < ROLLS / 6, "far too many herb seeds: " + byHand);
        check(herbSeeds(helper, player, new ItemStack(Items.SHEARS)) == 0, "shears foraged seeds");
        ItemStack silk = new ItemStack(Items.DIAMOND_HOE);
        silk.enchant(Enchantments.SILK_TOUCH, 1);
        check(herbSeeds(helper, player, silk) == 0, "silk touch foraged seeds");
        check(herbSeeds(helper, null, ItemStack.EMPTY) == 0, "grass broken without a player foraged seeds");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void conditionsAreLoadedFromData(GameTestHelper helper) {
        HerbConditions current = HerbConditionReloadListener.current();
        for (HerbSpec herb : HerbCatalog.ALL) {
            check(HerbConditions.defaultFor(herb).equals(current.get(herb)), herb.id() + " conditions " + current.get(herb));
        }
        helper.succeed();
    }

    private static int herbSeeds(GameTestHelper helper, Entity breaker, ItemStack tool) {
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(new BlockPos(4, 1, 4));
        int seeds = 0;
        for (int i = 0; i < ROLLS; i++) {
            LootParams.Builder params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(abs))
                    .withParameter(LootContextParams.TOOL, tool)
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, breaker);
            List<ItemStack> drops = Blocks.GRASS.defaultBlockState().getDrops(params);
            for (ItemStack drop : drops) {
                if (drop.is(BotanyTags.SEEDS)) {
                    seeds += drop.getCount();
                }
            }
        }
        return seeds;
    }
}
