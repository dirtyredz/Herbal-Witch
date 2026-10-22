package io.github.dirtyredz.herbalwitch.apothecary.stations.process;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.drying_rack.block.DryingRackBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.kettle.block.KettleBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.mortar.block.MortarBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;

import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.inventory;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.place;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.tick;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.water;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.clearDrops;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.dropped;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * The shared processing contract through real capability and menu paths: reserved inputs stay put,
 * automation sides, persistence without catch-up, recipe changes cancelling, contents dropping once.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ProcessBehaviourGameTests {
    private static final BlockPos AT = new BlockPos(4, 2, 4);

    private ProcessBehaviourGameTests() {
    }

    private static IItemHandler side(GameTestHelper helper, Direction side) {
        return helper.getBlockEntity(AT).getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElseThrow(IllegalStateException::new);
    }

    @GameTest(template = TEMPLATE)
    public static void automationFeedsIngredientsFromAboveAndTakesProductsBelow(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(AT.below(), Blocks.STONE);
        place(helper, StationBlocks.KETTLE.get(), AT);
        IItemHandler top = side(helper, Direction.UP);
        IItemHandler sideways = side(helper, Direction.NORTH);
        IItemHandler bottom = side(helper, Direction.DOWN);
        check(top.insertItem(0, new ItemStack(PreparationItems.powder(HerbCatalog.MINT)), false).isEmpty(), "top refused an ingredient");
        check(!top.insertItem(1, new ItemStack(Items.DIRT), false).isEmpty(), "top accepted a non-ingredient");
        check(sideways.insertItem(0, water(), false).isEmpty(), "side refused water");
        check(bottom.getSlots() == 2 && bottom.insertItem(0, new ItemStack(Items.SUGAR), false).getCount() == 1, "bottom accepted insertion");
        check(top.extractItem(0, 1, false).isEmpty(), "ingredients extracted from above");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void reservedInputsCannotBeExtractedOrSwappedButExcessCan(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(AT.below(), Blocks.STONE);
        place(helper, StationBlocks.DRYING_RACK.get(), AT);
        ProcessInventory inv = inventory(helper, AT);
        inv.setStackInSlot(DryingRackBlockEntity.INPUT, new ItemStack(BotanyItems.fresh(HerbCatalog.MINT), 3));
        tick(helper, AT, 10);
        check(inv.reserved(DryingRackBlockEntity.INPUT) == 1, "one herb reserved, not " + inv.reserved(DryingRackBlockEntity.INPUT));
        // Through a menu: shift-click moves only the excess; a different item cannot swap in.
        ServerPlayer player = player(helper, "rack_user", GameType.SURVIVAL, Direction.NORTH);
        AbstractContainerMenu menu = ((DryingRackBlockEntity) helper.getBlockEntity(AT)).createMenu(1, player.getInventory(), player);
        menu.quickMoveStack(player, 0);
        check(inv.getStackInSlot(DryingRackBlockEntity.INPUT).getCount() == 1, "shift-click took reserved herbs: "
                + inv.getStackInSlot(DryingRackBlockEntity.INPUT));
        check(player.getInventory().countItem(BotanyItems.fresh(HerbCatalog.MINT)) == 2, "excess not moved to the player");
        menu.setCarried(new ItemStack(BotanyItems.fresh(HerbCatalog.SAGE)));
        menu.clicked(0, 0, ClickType.PICKUP, player);
        check(inv.getStackInSlot(DryingRackBlockEntity.INPUT).is(BotanyItems.fresh(HerbCatalog.MINT)), "reserved herb swapped out");
        menu.setCarried(ItemStack.EMPTY);
        menu.clicked(0, 0, ClickType.PICKUP, player);
        check(inv.getStackInSlot(DryingRackBlockEntity.INPUT).getCount() == 1, "click took a reserved herb");
        // Automation cannot take it either.
        check(side(helper, Direction.DOWN).extractItem(0, 64, false).isEmpty(), "bottom extracted an input");
        tick(helper, AT, 1_200);
        check(inv.getStackInSlot(DryingRackBlockEntity.OUTPUT).getCount() == 1, "drying did not complete exactly once");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aSavedJobResumesWithoutCatchUp(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(AT.below(), Blocks.STONE);
        place(helper, StationBlocks.MORTAR.get(), AT);
        MortarBlockEntity mortar = (MortarBlockEntity) helper.getBlockEntity(AT);
        mortar.inventory().setStackInSlot(MortarBlockEntity.INPUT_0, new ItemStack(Items.BONE));
        tick(helper, AT, 60);
        CompoundTag saved = mortar.saveWithoutMetadata();
        // A fresh block entity loaded from the save (as after a restart or chunk reload).
        helper.setBlock(AT, Blocks.AIR);
        place(helper, StationBlocks.MORTAR.get(), AT);
        MortarBlockEntity reloaded = (MortarBlockEntity) helper.getBlockEntity(AT);
        reloaded.load(saved);
        check(reloaded.inventory().getStackInSlot(MortarBlockEntity.INPUT_0).is(Items.BONE), "inventory lost");
        tick(helper, AT, 39);
        check(reloaded.inventory().getStackInSlot(MortarBlockEntity.OUTPUT).isEmpty(), "caught up or restarted wrongly");
        tick(helper, AT, 1);
        check(reloaded.inventory().getStackInSlot(MortarBlockEntity.OUTPUT).getCount() == 3, "progress was not kept");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aChangedRecipeCancelsTheJobAndKeepsTheItems(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(AT.below(), Blocks.STONE);
        place(helper, StationBlocks.MORTAR.get(), AT);
        MortarBlockEntity mortar = (MortarBlockEntity) helper.getBlockEntity(AT);
        mortar.inventory().setStackInSlot(MortarBlockEntity.INPUT_0, new ItemStack(Items.BONE));
        tick(helper, AT, 60);
        CompoundTag saved = mortar.saveWithoutMetadata();
        saved.getCompound("Job").putString("Fingerprint", "an older version of the recipe");
        mortar.load(saved);
        tick(helper, AT, 99);
        check(mortar.inventory().getStackInSlot(MortarBlockEntity.OUTPUT).isEmpty(), "kept progress from a changed recipe");
        check(mortar.inventory().getStackInSlot(MortarBlockEntity.INPUT_0).is(Items.BONE), "cancel lost the ingredient");
        tick(helper, AT, 1);
        check(mortar.inventory().getStackInSlot(MortarBlockEntity.OUTPUT).getCount() == 3, "did not restart cleanly");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void breakingOrLosingSupportDropsEachStackOnce(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(AT.below(), Blocks.STONE);
        place(helper, StationBlocks.KETTLE.get(), AT);
        ProcessInventory inv = inventory(helper, AT);
        inv.setStackInSlot(KettleBlockEntity.INPUT_0, new ItemStack(PreparationItems.powder(HerbCatalog.MINT), 5));
        inv.setStackInSlot(KettleBlockEntity.INPUT_1, new ItemStack(Items.SUGAR, 2));
        inv.setStackInSlot(KettleBlockEntity.WATER, water());
        tick(helper, AT, 5); // reserves, then waits for heat
        clearDrops(helper);
        helper.setBlock(AT.below(), Blocks.AIR);
        check(helper.getBlockState(AT).isAir(), "kettle floats without support");
        check(dropped(helper, PreparationItems.powder(HerbCatalog.MINT)) == 5, "powder dropped " + dropped(helper, PreparationItems.powder(HerbCatalog.MINT)));
        check(dropped(helper, Items.SUGAR) == 2 && dropped(helper, Items.POTION) == 1, "inputs not dropped once");
        check(dropped(helper, StationBlocks.KETTLE.get().asItem()) == 1, "the kettle itself did not drop");
        helper.succeed();
    }
}
