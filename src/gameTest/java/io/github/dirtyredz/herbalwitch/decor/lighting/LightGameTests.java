package io.github.dirtyredz.herbalwitch.decor.lighting;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.behavior.lighting.DecorLightBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.LightingCatalog;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorItems;
import io.github.dirtyredz.herbalwitch.decor.structure.FurnitureLayouts;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.apothecary.stations.heat.HeatSourceTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.click;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.useItemOn;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.clearDrops;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.dropped;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * Lights: unlit when placed, switched by hand, light 14 on every cell while lit (in a sealed room, so
 * no beacon light leaks in), the state kept across save, never a heat source, and a hanging lamp
 * falling when its ceiling goes.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LightGameTests {
    private LightGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void everyLightIsUnlitToggledAndNeverAHeatSource(GameTestHelper helper) {
        for (FurnitureSpec spec : FurnitureCatalog.withBehavior(FurnitureBehavior.LIGHT)) {
            BlockState state = DecorBlocks.block(spec).defaultBlockState();
            check(state.getBlock() instanceof DecorLightBlock && !state.getValue(DecorLightBlock.LIT), spec.id() + " starts lit");
            check(state.setValue(DecorLightBlock.LIT, true).getLightEmission() == 14, spec.id() + " lit emission");
            check(!state.is(HeatSourceTags.HEAT_SOURCES), spec.id() + " heats");
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void aFloorLampLightsEveryCellInASealedRoom(GameTestHelper helper) {
        floor(helper);
        FurnitureSpec lamp = LightingCatalog.FLOOR_LAMP; // two high
        BlockPos at = new BlockPos(4, 1, 4);
        // sealed stone room 5 x 4 x 5 around the lamp
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 0; dy <= 4; dy++) {
                    if (Math.abs(dx) == 2 || Math.abs(dz) == 2 || dy == 4) {
                        helper.setBlock(at.offset(dx, dy, dz), Blocks.STONE);
                    }
                }
            }
        }
        ServerPlayer player = player(helper, "lamp_switcher", GameType.SURVIVAL, Direction.SOUTH);
        player.moveTo(helper.absolutePos(at.north()).getX() + 0.5, helper.absolutePos(at).getY(), helper.absolutePos(at.north()).getZ() + 0.5);
        check(useItemOn(helper, player, new ItemStack(DecorItems.item(lamp)), at.below(), Direction.UP).consumesAction(), "lamp not placed");
        check(click(helper, player, at.above()).consumesAction(), "clicking the upper cell did nothing");
        helper.startSequence()
                .thenWaitUntil(() -> {
                    check(helper.getBlockState(at).getValue(DecorLightBlock.LIT) && helper.getBlockState(at.above()).getValue(DecorLightBlock.LIT),
                            "not every cell is lit");
                    check(helper.getLevel().getBrightness(LightLayer.BLOCK, helper.absolutePos(at.above())) == 14, "block light "
                            + helper.getLevel().getBrightness(LightLayer.BLOCK, helper.absolutePos(at.above())));
                })
                .thenExecute(() -> check(click(helper, player, at).consumesAction(), "second click did nothing"))
                .thenWaitUntil(() -> check(!helper.getBlockState(at.above()).getValue(DecorLightBlock.LIT)
                        && helper.getLevel().getBrightness(LightLayer.BLOCK, helper.absolutePos(at.above())) == 0, "still lit"))
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE)
    public static void aHangingLampFallsWithItsCeiling(GameTestHelper helper) {
        FurnitureSpec pendant = LightingCatalog.PENDANT_LIGHT; // two high, hangs from its top cell
        BlockPos ceiling = new BlockPos(4, 5, 4);
        helper.setBlock(ceiling, Blocks.STONE);
        ServerPlayer player = player(helper, "lamp_hanger", GameType.SURVIVAL, Direction.SOUTH);
        check(useItemOn(helper, player, new ItemStack(DecorItems.item(pendant)), ceiling, Direction.DOWN).consumesAction(), "not hung");
        MultiblockLayout layout = FurnitureLayouts.of(pendant);
        List<BlockPos> cells = new ArrayList<>(List.of(ceiling.below(), ceiling.below(2)));
        cells.forEach(c -> check(helper.getBlockState(c).is(DecorBlocks.block(pendant)), "no pendant cell at " + c));
        check(layout.size() == 2, "pendant cells");
        clearDrops(helper);
        helper.setBlock(ceiling, Blocks.AIR);
        cells.forEach(c -> check(helper.getBlockState(c).isAir(), "the pendant hangs from nothing at " + c));
        check(dropped(helper, DecorItems.item(pendant)) == 1, "pendant dropped " + dropped(helper, DecorItems.item(pendant)));
        helper.succeed();
    }
}
