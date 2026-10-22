package io.github.dirtyredz.herbalwitch.decor.storage;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.behavior.storage.FurnitureInventory;
import io.github.dirtyredz.herbalwitch.decor.behavior.storage.FurnitureStorageEntity;
import io.github.dirtyredz.herbalwitch.decor.behavior.storage.FurnitureStorageMenu;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.StorageCatalog;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorItems;
import io.github.dirtyredz.herbalwitch.decor.structure.FurnitureLayouts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.click;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.clearDrops;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.dropped;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * Cabinets and displays: one origin inventory of the catalogue's size opened from any cell, quick
 * move with display slots holding one item, automation through any cabinet cell (none for
 * displays), versioned save and reload, contents dropped exactly once, a pre-0.4.0 shelf starting
 * empty, and the display's synchronised contents.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StorageGameTests {
    private static final BlockPos ORIGIN = new BlockPos(4, 1, 4);

    private StorageGameTests() {
    }

    static void put(GameTestHelper helper, FurnitureSpec spec) {
        var block = DecorBlocks.block(spec);
        var layout = FurnitureLayouts.of(spec);
        BlockState origin = block.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
        if (layout == null) {
            helper.setBlock(ORIGIN, origin);
            return;
        }
        for (int i = 0; i < layout.size(); i++) {
            helper.setBlock(ORIGIN.offset(layout.offset(Direction.NORTH, i)), origin.setValue(layout.part(), i));
        }
    }

    static FurnitureStorageEntity origin(GameTestHelper helper) {
        BlockEntity entity = helper.getBlockEntity(ORIGIN);
        check(entity instanceof FurnitureStorageEntity, "no storage entity at the origin: " + entity);
        return (FurnitureStorageEntity) entity;
    }

    @GameTest(template = TEMPLATE)
    public static void everyStoragePieceOpensItsCatalogueSizedMenuFromAnyCell(GameTestHelper helper) {
        floor(helper);
        ServerPlayer player = player(helper, "opener", GameType.ADVENTURE, Direction.SOUTH); // using is not building
        for (FurnitureSpec spec : FurnitureCatalog.ALL) {
            if (spec.behavior() != FurnitureBehavior.STORAGE && spec.behavior() != FurnitureBehavior.DISPLAY) {
                continue;
            }
            for (int x = 0; x < 9; x++) {
                for (int z = 0; z < 9; z++) {
                    for (int y = 1; y < 6; y++) {
                        helper.setBlock(new BlockPos(x, y, z), net.minecraft.world.level.block.Blocks.AIR);
                    }
                }
            }
            if (spec.support() != io.github.dirtyredz.herbalwitch.decor.catalog.MountSpec.Support.WALL) {
                put(helper, spec);
            } else {
                helper.setBlock(ORIGIN.south(), net.minecraft.world.level.block.Blocks.STONE);
                put(helper, spec);
            }
            var layout = FurnitureLayouts.of(spec);
            BlockPos last = layout == null ? ORIGIN : ORIGIN.offset(layout.offset(Direction.NORTH, layout.size() - 1));
            player.closeContainer();
            check(click(helper, player, last).consumesAction(), spec.id() + ": using its last cell did nothing");
            check(player.containerMenu instanceof FurnitureStorageMenu menu && menu.spec().slots() == spec.slots()
                    && menu.slots.size() == spec.slots() + 36, spec.id() + ": menu " + player.containerMenu);
            player.closeContainer();
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void quickMoveFillsCabinetsAndOneItemPerDisplaySlot(GameTestHelper helper) {
        floor(helper);
        ServerPlayer player = player(helper, "mover", GameType.SURVIVAL, Direction.SOUTH);
        put(helper, StorageCatalog.LOWER_CABINET);
        check(click(helper, player, ORIGIN).consumesAction(), "cabinet did not open");
        FurnitureStorageMenu cabinet = (FurnitureStorageMenu) player.containerMenu;
        player.getInventory().setItem(9, new ItemStack(Items.BREAD, 40));
        int playerSlot = 27; // the first main-inventory slot follows the 27 cabinet slots
        cabinet.quickMoveStack(player, playerSlot);
        check(origin(helper).inventory().getStackInSlot(0).getCount() == 40, "cabinet holds " + origin(helper).inventory().getStackInSlot(0));
        player.closeContainer();
        for (int x = 0; x < 9; x++) {
            helper.setBlock(new BlockPos(x, 1, 4), net.minecraft.world.level.block.Blocks.AIR);
            helper.setBlock(new BlockPos(x, 2, 4), net.minecraft.world.level.block.Blocks.AIR);
        }
        put(helper, StorageCatalog.OPEN_SHELVES);
        check(click(helper, player, ORIGIN).consumesAction(), "display did not open");
        FurnitureStorageMenu display = (FurnitureStorageMenu) player.containerMenu;
        player.getInventory().setItem(9, new ItemStack(Items.GLASS_BOTTLE, 12));
        ItemStack moved;
        do {
            moved = display.quickMoveStack(player, 9);
        } while (!moved.isEmpty() && !player.getInventory().getItem(9).isEmpty());
        FurnitureInventory shelf = origin(helper).inventory();
        for (int i = 0; i < shelf.getSlots(); i++) {
            check(shelf.getStackInSlot(i).getCount() == 1, "display slot " + i + " holds " + shelf.getStackInSlot(i));
        }
        check(player.getInventory().getItem(9).getCount() == 3, "player keeps " + player.getInventory().getItem(9));
        player.closeContainer();
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void hoppersReachACabinetThroughAnyCellButNotADisplay(GameTestHelper helper) {
        floor(helper);
        put(helper, StorageCatalog.CHINA_CABINET);
        BlockPos upper = ORIGIN.above().west();
        IItemHandler handler = helper.getBlockEntity(upper).getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        check(handler != null && handler.getSlots() == 54, "no 54-slot handler on an upper cell");
        check(ItemHandlerHelper.insertItem(handler, new ItemStack(Items.APPLE, 5), false).isEmpty(), "insert refused");
        check(origin(helper).inventory().contents().get(0).getCount() == 5, "the origin does not hold what the upper cell took");
        helper.setBlock(new BlockPos(1, 1, 1), DecorBlocks.block(StorageCatalog.POTION_SHELF).defaultBlockState());
        BlockEntity shelf = helper.getBlockEntity(new BlockPos(1, 1, 1));
        check(shelf != null && !shelf.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent(), "a display offers automation");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void contentsSurviveSaveAndAForeignSchemaLoadsEmpty(GameTestHelper helper) {
        floor(helper);
        put(helper, StorageCatalog.PANTRY);
        FurnitureStorageEntity pantry = origin(helper);
        pantry.inventory().setStackInSlot(3, new ItemStack(Items.CARROT, 7));
        CompoundTag saved = pantry.saveWithFullMetadata();
        pantry.inventory().takeAll();
        pantry.load(saved);
        check(pantry.inventory().getStackInSlot(3).getCount() == 7, "reloaded " + pantry.inventory().getStackInSlot(3));
        CompoundTag future = saved.copy();
        future.getCompound("Inventory").putInt("Schema", 99);
        pantry.load(future);
        check(pantry.inventory().contents().isEmpty(), "an unknown schema produced items");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void breakingAnyCellDropsTheContentsOnce(GameTestHelper helper) {
        floor(helper);
        put(helper, StorageCatalog.SIDEBOARD);
        origin(helper).inventory().setStackInSlot(0, new ItemStack(Items.EMERALD, 9));
        clearDrops(helper);
        helper.getLevel().destroyBlock(helper.absolutePos(ORIGIN.west()), true);
        check(dropped(helper, Items.EMERALD) == 9, "emeralds dropped " + dropped(helper, Items.EMERALD));
        check(dropped(helper, DecorItems.item(StorageCatalog.SIDEBOARD)) == 1, "sideboards dropped " + dropped(helper, DecorItems.item(StorageCatalog.SIDEBOARD)));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void anOldShelfStartsEmptyAndADisplaySyncsWhatItShows(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(ORIGIN.south(), net.minecraft.world.level.block.Blocks.STONE);
        // a 0.1.0 shelf: the block without any saved entity data
        helper.setBlock(ORIGIN, DecorBlocks.block(StorageCatalog.SMALL_WALL_SHELF).defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
        FurnitureStorageEntity shelf = origin(helper);
        check(shelf.inventory().getSlots() == 3 && shelf.inventory().contents().isEmpty(), "an old shelf did not start empty");
        shelf.inventory().setStackInSlot(1, new ItemStack(Items.GLASS_BOTTLE));
        CompoundTag sync = shelf.getUpdateTag();
        check(sync.contains("Shown") && sync.getList("Shown", 10).size() == 1, "the update tag does not carry the shown item: " + sync);
        check(shelf.getUpdatePacket() != null, "no update packet for a display");
        helper.succeed();
    }
}
