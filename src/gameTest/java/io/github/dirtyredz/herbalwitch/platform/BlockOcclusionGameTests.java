package io.github.dirtyredz.herbalwitch.platform;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;

/**
 * Guard for the herb-bed see-through bug (0.2.0-dev.3): a block that is not a full cube must not
 * occlude, or its neighbours cull the faces it only partly covers and the world shows through.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BlockOcclusionGameTests {
    private BlockOcclusionGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void onlyFullCubesOccludeNeighbourFaces(GameTestHelper helper) {
        List<String> offenders = new ArrayList<>();
        int checked = 0;
        for (Block block : ForgeRegistries.BLOCKS) {
            if (!HerbalWitch.MOD_ID.equals(ForgeRegistries.BLOCKS.getKey(block).getNamespace())) {
                continue;
            }
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                checked++;
                if (state.canOcclude() && !Block.isShapeFullBlock(state.getOcclusionShape(helper.getLevel(), BlockPos.ZERO))) {
                    offenders.add(ForgeRegistries.BLOCKS.getKey(block).getPath());
                    break;
                }
            }
        }
        if (checked == 0) {
            throw new GameTestAssertException("no herbalwitch block states were checked");
        }
        if (!offenders.isEmpty()) {
            throw new GameTestAssertException("non-full-cube blocks that occlude (add noOcclusion): " + offenders);
        }
        helper.succeed();
    }
}
