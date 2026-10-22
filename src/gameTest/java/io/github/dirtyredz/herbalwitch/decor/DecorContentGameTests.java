package io.github.dirtyredz.herbalwitch.decor;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.behavior.DecorDoorBlock;
import io.github.dirtyredz.herbalwitch.decor.behavior.sleeping.DecorBedBlock;
import io.github.dirtyredz.herbalwitch.decor.structure.DecorAssemblyBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.Wave;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.LightingCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.SeatingCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.StorageCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.SurfacesCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.TextilesCatalog;
import io.github.dirtyredz.herbalwitch.decor.structure.FurniturePlacementItem;
import io.github.dirtyredz.herbalwitch.decor.structure.FurnitureLayouts;
import io.github.dirtyredz.herbalwitch.decor.behavior.DecorLightBlock;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorCreativeTab;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorItems;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorTags;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.github.dirtyredz.herbalwitch.fixture.BotanyFixtures.grid;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/** Registries, items, the creative tab, loaded recipes, tags and loot, as a dedicated server sees them. */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DecorContentGameTests {
    /** Registered pieces after each wave, written independently of the catalogue. */
    private static final Map<Wave, Integer> DELIVERED = Map.of(Wave.M1, 5, Wave.A, 27, Wave.B, 57, Wave.C, 94);

    private DecorContentGameTests() {
    }

    /** Decor's own ids; other features share the namespace (the seat entity is not a block). */
    private static boolean isDecor(ResourceLocation id) {
        return id.getNamespace().equals(HerbalWitch.MOD_ID) && id.getPath().startsWith("decor/");
    }

    @GameTest(template = TEMPLATE)
    public static void everyDeliveredPieceUnderItsStableId(GameTestHelper helper) {
        int expected = DELIVERED.get(FurnitureCatalog.DELIVERED);
        long blocks = ForgeRegistries.BLOCKS.getKeys().stream().filter(DecorContentGameTests::isDecor).count();
        long items = ForgeRegistries.ITEMS.getKeys().stream().filter(DecorContentGameTests::isDecor).count();
        check(blocks == expected && items == expected, "expected " + expected + " decor blocks and items, found " + blocks + " / " + items);
        for (FurnitureSpec spec : FurnitureCatalog.ALL) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "decor/" + spec.family().directory() + "/" + spec.id());
            Block block = ForgeRegistries.BLOCKS.getValue(id);
            check(block == DecorBlocks.block(spec), "missing or wrong block " + id);
            check(ForgeRegistries.ITEMS.getValue(id) == block.asItem() && block.asItem() != Items.AIR, "missing item " + id);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void eachPieceUsesItsBehaviourBlock(GameTestHelper helper) {
        for (FurnitureSpec spec : FurnitureCatalog.ALL) {
            Block block = DecorBlocks.block(spec);
            boolean ok = switch (spec.behavior()) {
                case LIGHT -> block instanceof DecorLightBlock;
                case BED -> block instanceof DecorBedBlock;
                case DOOR -> block instanceof DecorDoorBlock;
                case RUG -> spec.multiblock() ? block instanceof DecorAssemblyBlock : block.getClass() == CarpetBlock.class;
                default -> block instanceof DecorAssemblyBlock;
            };
            check(ok, spec.id() + " is a " + block.getClass().getSimpleName());
            check((spec == TextilesCatalog.SMALL_RUG) != (block.asItem() instanceof FurniturePlacementItem), spec.id() + " item");
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void creativeTabHoldsEveryPieceOnce(GameTestHelper helper) {
        CreativeModeTab tab = DecorCreativeTab.DECOR.get();
        tab.buildContents(new CreativeModeTab.ItemDisplayParameters(helper.getLevel().enabledFeatures(), false,
                helper.getLevel().registryAccess()));
        List<Item> shown = tab.getDisplayItems().stream().map(ItemStack::getItem).toList();
        check(shown.equals(DecorItems.all()) && shown.size() == DELIVERED.get(FurnitureCatalog.DELIVERED), "tab shows " + shown);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void craftingMakesEveryPieceAndNothingRestyles(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack e = ItemStack.EMPTY;
        ItemStack planks = new ItemStack(Items.DARK_OAK_PLANKS);
        ItemStack slab = new ItemStack(Items.DARK_OAK_SLAB);
        ItemStack wool = new ItemStack(Items.WHITE_WOOL);
        ItemStack stick = new ItemStack(Items.STICK);
        expect(level, grid(planks, new ItemStack(Items.PINK_WOOL), planks, planks, planks, planks, stick, e, stick), SeatingCatalog.ARMCHAIR, 1);
        expect(level, grid(slab, slab, slab, planks, new ItemStack(Items.GOLD_NUGGET), planks, planks, planks, planks),
                SurfacesCatalog.APOTHECARY_COUNTER, 1);
        expect(level, grid(slab, slab, slab, stick, e, stick, e, e, e), StorageCatalog.SMALL_WALL_SHELF, 2);
        expect(level, grid(wool, e, e, new ItemStack(Items.TORCH), e, e, slab, e, e), LightingCatalog.TABLE_LAMP, 1);
        expect(level, grid(new ItemStack(Items.GREEN_CARPET), new ItemStack(Items.STRING), new ItemStack(Items.RED_CARPET),
                e, e, e, e, e, e), TextilesCatalog.SMALL_RUG, 2);
        // exactly one crafting recipe per delivered piece, making that piece
        Map<Item, Integer> recipes = new HashMap<>();
        level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING).stream().filter(r -> isDecor(r.getId()))
                .forEach(r -> recipes.merge(r.getResultItem(level.registryAccess()).getItem(), 1, Integer::sum));
        for (FurnitureSpec spec : FurnitureCatalog.ALL) {
            check(recipes.getOrDefault(DecorItems.item(spec), 0) == 1, spec.id() + ": " + recipes.getOrDefault(DecorItems.item(spec), 0) + " recipes");
        }
        check(recipes.values().stream().mapToInt(Integer::intValue).sum() == FurnitureCatalog.ALL.size(), "stray decor recipes " + recipes);
        long stonecutting = level.getRecipeManager().getAllRecipesFor(RecipeType.STONECUTTING).stream()
                .filter(r -> r.getId().getNamespace().equals(HerbalWitch.MOD_ID)).count();
        check(stonecutting == 0, "restyling recipes remain: " + stonecutting);
        helper.succeed();
    }

    /** Every decor recipe's own grid (first choice of each ingredient) crafts that recipe and no other, vanilla's included. */
    @GameTest(template = TEMPLATE)
    public static void everyDecorGridIsUnambiguous(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (CraftingRecipe recipe : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            if (!isDecor(recipe.getId()) || !(recipe instanceof net.minecraft.world.item.crafting.ShapedRecipe shaped)) {
                continue;
            }
            ItemStack[] cells = new ItemStack[9];
            java.util.Arrays.fill(cells, ItemStack.EMPTY);
            for (int y = 0; y < shaped.getHeight(); y++) {
                for (int x = 0; x < shaped.getWidth(); x++) {
                    var ingredient = shaped.getIngredients().get(x + y * shaped.getWidth());
                    cells[x + y * 3] = ingredient.isEmpty() ? ItemStack.EMPTY : ingredient.getItems()[0].copy();
                }
            }
            TransientCraftingContainer grid = grid(cells[0], cells[1], cells[2], cells[3], cells[4], cells[5], cells[6], cells[7], cells[8]);
            List<CraftingRecipe> matches = level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, grid, level);
            check(matches.size() == 1 && matches.get(0).getId().equals(recipe.getId()),
                    recipe.getId() + " grid also matches " + matches.stream().map(CraftingRecipe::getId).toList());
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void tagsAreLoaded(GameTestHelper helper) {
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            Block block = entry.block().get();
            check(block.defaultBlockState().is(DecorTags.FURNITURE), block + " furniture tag");
            check(new ItemStack(block).is(DecorTags.FURNITURE_ITEMS), block + " furniture item tag");
            boolean rug = entry.spec().behavior() == FurnitureBehavior.RUG;
            check(block.defaultBlockState().is(BlockTags.MINEABLE_WITH_AXE) != rug, block + " axe tag");
        }
        helper.succeed();
    }

    /** Only the origin cell (a door's lower half) drops the piece, so breaking any part yields one item. */
    @GameTest(template = TEMPLATE)
    public static void everyPieceDropsItselfOnceFromItsOrigin(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        for (FurnitureSpec spec : FurnitureCatalog.ALL) {
            Block block = DecorBlocks.block(spec);
            List<ItemStack> drops = Block.getDrops(block.defaultBlockState(), helper.getLevel(), pos, null);
            check(drops.size() == 1 && drops.get(0).is(block.asItem()) && drops.get(0).getCount() == 1, spec.id() + " loot is " + drops);
            MultiblockLayout layout = FurnitureLayouts.of(spec);
            if (layout != null && block instanceof DecorAssemblyBlock assembly) {
                for (int i = 1; i < layout.size(); i++) {
                    BlockState cell = assembly.cellState(block.defaultBlockState(), i);
                    check(Block.getDrops(cell, helper.getLevel(), pos, null).isEmpty(), spec.id() + " cell " + i + " drops");
                }
            }
        }
        helper.succeed();
    }

    private static void expect(ServerLevel level, TransientCraftingContainer grid, FurnitureSpec spec, int count) {
        Optional<CraftingRecipe> recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level);
        check(recipe.isPresent(), spec.id() + ": no recipe matched");
        ItemStack out = recipe.get().assemble(grid, level.registryAccess());
        check(out.is(DecorItems.item(spec)) && out.getCount() == count, spec.id() + ": produced " + out);
    }
}
