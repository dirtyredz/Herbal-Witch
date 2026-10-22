package io.github.dirtyredz.herbalwitch.data.client.decor.state;

import io.github.dirtyredz.herbalwitch.data.client.decor.DecorModelProvider;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.client.model.generators.ConfiguredModel;

/**
 * The door, with vanilla's facing, half, hinge and open states and only legal y turns. The project
 * is drawn closed, left-hinged, facing north (the leaf against the south edge, z 13..16, hinge at
 * x 0); the compiler adds its mirror image ({@code mirrored_<n>}, the right-hinged leaf). As in
 * vanilla, an open door is the opposite hand's leaf turned a quarter: open left is the mirrored
 * leaf turned 90 degrees, open right the authored leaf turned 270, which swings each leaf about its
 * hinge and keeps the handle away from it. Collision is vanilla's door shapes, which these match.
 */
public final class DoorStates {
    private DoorStates() {
    }

    public static void add(DecorModelProvider provider, FurnitureSpec spec, Block block) {
        provider.getVariantBuilder(block).forAllStatesExcept(state -> {
            int half = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? 0 : 1;
            boolean right = state.getValue(DoorBlock.HINGE) == DoorHingeSide.RIGHT;
            boolean open = state.getValue(DoorBlock.OPEN);
            boolean mirrored = right != open;
            int turn = open ? (right ? 270 : 90) : 0;
            int y = (DecorModelProvider.rotationY(state.getValue(DoorBlock.FACING)) + turn) % 360;
            return ConfiguredModel.builder()
                    .modelFile(provider.existing(DecorModelProvider.partModel(spec, mirrored ? "mirrored" : "part", half)))
                    .rotationY(y).build();
        }, DoorBlock.POWERED);
    }
}
