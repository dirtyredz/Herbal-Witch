package io.github.dirtyredz.herbalwitch.decor;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.block.TableLampBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.ArtSpikeCatalog;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.click;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.placeOnFloor;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;

/** The lamp switch and its persistence. */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DecorInteractionGameTests {
    private DecorInteractionGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void usingTheLampSwitchesItAndItsLight(GameTestHelper helper) {
        floor(helper);
        BlockPos lamp = new BlockPos(4, 1, 4);
        BlockPos abs = helper.absolutePos(lamp);
        ServerPlayer player = player(helper, "switch", GameType.SURVIVAL, Direction.NORTH);
        check(placeOnFloor(helper, player, DecorItems.item(ArtSpikeCatalog.TABLE_LAMP), lamp).consumesAction(), "lamp not placed");
        enclose(helper, lamp);
        helper.startSequence()
                .thenExecute(() -> {
                    check(click(helper, player, lamp).consumesAction(), "click not handled");
                    BlockState on = helper.getBlockState(lamp);
                    check(on.getValue(TableLampBlock.LIT) && on.getLightEmission() == TableLampBlock.LIGHT, "lamp not lit: " + on);
                })
                .thenWaitUntil(() -> check(helper.getLevel().getBrightness(LightLayer.BLOCK, abs) == TableLampBlock.LIGHT,
                        "block light " + helper.getLevel().getBrightness(LightLayer.BLOCK, abs)))
                .thenExecute(() -> {
                    check(click(helper, player, lamp).consumesAction(), "second click not handled");
                    BlockState off = helper.getBlockState(lamp);
                    check(!off.getValue(TableLampBlock.LIT) && off.getLightEmission() == 0, "lamp still lit: " + off);
                })
                .thenWaitUntil(() -> check(helper.getLevel().getBrightness(LightLayer.BLOCK, abs) == 0,
                        "block light stays " + helper.getLevel().getBrightness(LightLayer.BLOCK, abs)))
                .thenSucceed();
    }

    /**
     * Seals a 3 x 3 x 3 stone room around {@code centre} (on the floor), so the only block light inside
     * is the lamp's: neighbouring tests' pass/fail beacons otherwise leak light into this one.
     */
    private static void enclose(GameTestHelper helper, BlockPos centre) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 0; dy <= 3; dy++) {
                    boolean shell = Math.abs(dx) == 2 || Math.abs(dz) == 2 || dy == 3;
                    if (shell) {
                        helper.setBlock(centre.offset(dx, dy, dz), Blocks.STONE);
                    }
                }
            }
        }
    }

    @GameTest(template = TEMPLATE)
    public static void lampStateSurvivesSaveAndLoad(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (boolean lit : List.of(false, true)) {
            BlockState state = DecorBlocks.block(ArtSpikeCatalog.TABLE_LAMP).defaultBlockState().setValue(TableLampBlock.LIT, lit);
            CompoundTag saved = NbtUtils.writeBlockState(state);
            BlockState loaded = NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), saved);
            check(loaded == state, "lit=" + lit + " reloads as " + loaded);
        }
        helper.succeed();
    }
}
