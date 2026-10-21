package io.github.dirtyredz.herbalwitch.decor;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.block.HorizontalFurnitureBlock;
import io.github.dirtyredz.herbalwitch.decor.block.TableLampBlock;
import io.github.dirtyredz.herbalwitch.decor.block.WallShelfBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.ArtSpikeCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.Finish;
import io.github.dirtyredz.herbalwitch.decor.catalog.FinishCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorCreativeTab;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorItems;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorTags;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/** Registries, block items, the creative tab, loaded recipes, tags and loot, as a dedicated server sees them. */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DecorContentGameTests {
    private DecorContentGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void ninetyBlocksAndItemsUnderStableIds(GameTestHelper helper) {
        long blocks = ForgeRegistries.BLOCKS.getKeys().stream().filter(DecorContentGameTests::isDecor).count();
        long items = ForgeRegistries.ITEMS.getKeys().stream().filter(DecorContentGameTests::isDecor).count();
        check(blocks == 90, "expected 90 blocks, found " + blocks);
        check(items == 90, "expected 90 items, found " + items);
        Map<String, String> families = Map.of("armchair", "seating", "apothecary_counter", "surfaces",
                "small_wall_shelf", "storage", "table_lamp", "lighting", "small_rug", "textiles");
        for (FurnitureSpec spec : ArtSpikeCatalog.ALL) {
            for (Finish finish : FinishCatalog.ALL) {
                String palette = FinishCatalog.NATURAL.contains(finish) ? "natural" : "painted";
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID,
                        "decor/" + families.get(spec.id()) + "/" + spec.id() + "/" + palette + "/" + finish.id());
                Block block = ForgeRegistries.BLOCKS.getValue(id);
                check(block == DecorBlocks.block(spec, finish), "missing or wrong block " + id);
                check(ForgeRegistries.ITEMS.getValue(id) == block.asItem() && block.asItem() != Items.AIR, "missing item " + id);
            }
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void eachPieceUsesItsBehaviourBlock(GameTestHelper helper) {
        for (Finish finish : FinishCatalog.ALL) {
            check(DecorBlocks.block(ArtSpikeCatalog.ARMCHAIR, finish).getClass() == HorizontalFurnitureBlock.class, "armchair " + finish.id());
            check(DecorBlocks.block(ArtSpikeCatalog.APOTHECARY_COUNTER, finish).getClass() == HorizontalFurnitureBlock.class, "counter " + finish.id());
            check(DecorBlocks.block(ArtSpikeCatalog.SMALL_WALL_SHELF, finish) instanceof WallShelfBlock, "shelf " + finish.id());
            check(DecorBlocks.block(ArtSpikeCatalog.TABLE_LAMP, finish) instanceof TableLampBlock, "lamp " + finish.id());
            check(DecorBlocks.block(ArtSpikeCatalog.SMALL_RUG, finish).getClass() == CarpetBlock.class, "rug " + finish.id());
            // Finishes are cosmetic: every finish of a piece is equally strong.
            for (FurnitureSpec spec : ArtSpikeCatalog.ALL) {
                float base = DecorBlocks.block(spec, FinishCatalog.BASE).defaultDestroyTime();
                check(DecorBlocks.block(spec, finish).defaultDestroyTime() == base, spec.id() + " " + finish.id() + " hardness");
            }
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void creativeTabHoldsEveryItemOnce(GameTestHelper helper) {
        CreativeModeTab tab = DecorCreativeTab.DECOR.get();
        tab.buildContents(new CreativeModeTab.ItemDisplayParameters(helper.getLevel().enabledFeatures(), false,
                helper.getLevel().registryAccess()));
        List<Item> shown = tab.getDisplayItems().stream().map(ItemStack::getItem).toList();
        Set<Item> unique = new HashSet<>(shown);
        check(shown.size() == 90 && unique.size() == 90, "tab shows " + shown.size() + " stacks, " + unique.size() + " distinct");
        check(shown.equals(DecorItems.all()), "tab order differs from the catalogue order");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void craftingMakesTheDarkOakBaseOfEveryPiece(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack e = ItemStack.EMPTY;
        ItemStack planks = new ItemStack(Items.DARK_OAK_PLANKS);
        ItemStack slab = new ItemStack(Items.DARK_OAK_SLAB);
        ItemStack wool = new ItemStack(Items.WHITE_WOOL);
        ItemStack stick = new ItemStack(Items.STICK);
        expect(level, grid(planks, new ItemStack(Items.PINK_WOOL), planks, planks, planks, planks, stick, e, stick),
                ArtSpikeCatalog.ARMCHAIR, 1);
        expect(level, grid(slab, slab, slab, planks, new ItemStack(Items.GOLD_NUGGET), planks, planks, planks, planks),
                ArtSpikeCatalog.APOTHECARY_COUNTER, 1);
        expect(level, grid(slab, slab, slab, stick, e, stick, e, e, e), ArtSpikeCatalog.SMALL_WALL_SHELF, 2);
        expect(level, grid(wool, e, e, new ItemStack(Items.TORCH), e, e, slab, e, e), ArtSpikeCatalog.TABLE_LAMP, 1);
        expect(level, grid(new ItemStack(Items.GREEN_CARPET), new ItemStack(Items.STRING), new ItemStack(Items.RED_CARPET),
                e, e, e, e, e, e), ArtSpikeCatalog.SMALL_RUG, 2);
        long crafting = level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING).stream()
                .filter(r -> isDecor(r.getId())).count();
        long stonecutting = level.getRecipeManager().getAllRecipesFor(RecipeType.STONECUTTING).stream()
                .filter(r -> isDecor(r.getId())).count();
        check(crafting == 5, "expected 5 crafting recipes, loaded " + crafting);
        check(stonecutting == 90, "expected 90 restyling recipes, loaded " + stonecutting);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void tagsAreLoaded(GameTestHelper helper) {
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            Block block = entry.block().get();
            check(block.defaultBlockState().is(DecorTags.FURNITURE), block + " furniture tag");
            check(new ItemStack(block).is(DecorTags.FURNITURE_ITEMS), block + " furniture item tag");
            boolean rug = entry.spec() == ArtSpikeCatalog.SMALL_RUG;
            check(block.defaultBlockState().is(BlockTags.MINEABLE_WITH_AXE) != rug, block + " axe tag");
            for (FurnitureSpec spec : ArtSpikeCatalog.ALL) {
                check(new ItemStack(block).is(DecorTags.pieceItems(spec)) == (spec == entry.spec()), block + " in " + spec.id() + " tag");
            }
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void everyBlockDropsItsOwnAppearance(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        for (Block block : DecorBlocks.all()) {
            List<ItemStack> drops = Block.getDrops(block.defaultBlockState(), helper.getLevel(), pos, null);
            check(drops.size() == 1 && drops.get(0).is(block.asItem()) && drops.get(0).getCount() == 1, block + " loot is " + drops);
        }
        helper.succeed();
    }

    private static TransientCraftingContainer grid(ItemStack... stacks) {
        TransientCraftingContainer grid = new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {
            @Override
            public ItemStack quickMoveStack(Player player, int index) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        }, 3, 3);
        for (int i = 0; i < stacks.length; i++) {
            grid.setItem(i, stacks[i].copy());
        }
        return grid;
    }

    /** Decor's own ids; other features share the namespace. */
    private static boolean isDecor(ResourceLocation id) {
        return id.getNamespace().equals(HerbalWitch.MOD_ID) && id.getPath().startsWith("decor/");
    }

    private static void expect(ServerLevel level, TransientCraftingContainer grid, FurnitureSpec spec, int count) {
        Optional<CraftingRecipe> recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level);
        check(recipe.isPresent(), spec.id() + ": no recipe matched");
        ItemStack out = recipe.get().assemble(grid, level.registryAccess());
        check(out.is(DecorItems.item(spec, FinishCatalog.BASE)) && out.getCount() == count, spec.id() + ": produced " + out);
    }
}
