package io.github.dirtyredz.herbalwitch.apothecary.botany;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import static io.github.dirtyredz.herbalwitch.fixture.BotanyFixtures.bed;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.useItemOn;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/** All fourteen herbs exist under their permanent ids, and their seeds plant only on farmland or a herb bed. */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HerbPlantingGameTests {
    private HerbPlantingGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void fourteenHerbsUnderPermanentIds(GameTestHelper helper) {
        check(HerbCatalog.ALL.size() == 14, "catalogue holds " + HerbCatalog.ALL.size());
        for (HerbSpec herb : HerbCatalog.ALL) {
            String dir = "botany/" + herb.habitat().directory() + "/" + herb.id();
            check(ForgeRegistries.BLOCKS.getValue(id(dir + "/plant")) == BotanyBlocks.plant(herb), "plant " + dir);
            check(ForgeRegistries.ITEMS.getValue(id(dir + "/seeds")) == BotanyItems.seeds(herb), "seeds " + dir);
            check(ForgeRegistries.ITEMS.getValue(id(dir + "/fresh")) == BotanyItems.fresh(herb), "fresh " + dir);
            check(!ForgeRegistries.ITEMS.containsKey(id(dir + "/plant")), "the plant must not have its own item: " + dir);
            check(BotanyItems.seeds(herb).getDefaultInstance().is(BotanyTags.SEEDS), dir + " seeds tag");
            check(BotanyBlocks.plant(herb).defaultBlockState().is(BlockTags.CROPS), dir + " crops tag");
            check(BotanyBlocks.plant(herb).defaultBlockState().is(BlockTags.MAINTAINS_FARMLAND), dir + " farmland upkeep");
            check(!BotanyItems.fresh(herb).isEdible(), dir + " raw herbs are not food");
        }
        check(ForgeRegistries.BLOCKS.getValue(id("botany/garden/herb_bed")) == BotanyBlocks.herbBed(), "herb bed");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void seedsPlantOnFarmlandAndBedsOnly(GameTestHelper helper) {
        floor(helper);
        ServerPlayer player = player(helper, "planter", GameType.SURVIVAL, Direction.NORTH);
        int x = 0;
        for (HerbSpec herb : HerbCatalog.ALL) {
            int column = x % 7;
            int row = x / 7;
            BlockPos soil = new BlockPos(1 + column, 1, 1 + row * 3);
            if (x % 2 == 0) {
                helper.setBlock(soil, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7));
            } else {
                bed(helper, soil, true);
            }
            ItemStack seeds = new ItemStack(BotanyItems.seeds(herb), 2);
            InteractionResult planted = useItemOn(helper, player, seeds, soil, Direction.UP);
            check(planted.consumesAction() && seeds.getCount() == 1, herb.id() + " not planted: " + planted);
            check(helper.getBlockState(soil.above()).is(BotanyBlocks.plant(herb))
                    && helper.getBlockState(soil.above()).getValue(HerbCropBlock.AGE) == 0, herb.id() + " not a sprout");
            x++;
        }
        // Natural ground and stone refuse seeds, and nothing is consumed.
        for (var ground : java.util.List.of(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.STONE)) {
            BlockPos soil = new BlockPos(4, 1, 7);
            helper.setBlock(soil, ground);
            ItemStack seeds = new ItemStack(BotanyItems.seeds(HerbCatalog.SAGE));
            InteractionResult refused = useItemOn(helper, player, seeds, soil, Direction.UP);
            check(!refused.consumesAction() && seeds.getCount() == 1, "seeds planted on " + ground);
            check(helper.getBlockState(soil.above()).isAir(), "plant on " + ground);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aPlantPopsWhenItsSoilGoes(GameTestHelper helper) {
        floor(helper);
        BlockPos soil = new BlockPos(4, 1, 4);
        bed(helper, soil, true);
        helper.setBlock(soil.above(), BotanyBlocks.plant(HerbCatalog.MINT).getStateForAge(1));
        helper.setBlock(soil, Blocks.AIR);
        check(helper.getBlockState(soil.above()).isAir(), "plant floats without soil");
        helper.succeed();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, path);
    }
}
