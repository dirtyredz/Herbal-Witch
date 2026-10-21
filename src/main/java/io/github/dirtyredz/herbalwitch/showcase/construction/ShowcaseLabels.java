package io.github.dirtyredz.herbalwitch.showcase.construction;

import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Showcase signs: standing oak signs facing the entrance (north), waxed so nobody edits them by accident. */
public final class ShowcaseLabels {
    /** Rotation 8 shows the front text to someone standing to the north. */
    public static final BlockState SIGN = Blocks.OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, 8);

    private ShowcaseLabels() {
    }

    public static Component component(ShowcasePlacement.Line line) {
        return line.literal() ? Component.literal(line.text()) : Component.translatable(line.text(), line.args().toArray());
    }

    /** Places the sign and writes its lines. */
    public static void place(ServerLevel level, BlockPos pos, List<ShowcasePlacement.Line> lines) {
        level.setBlock(pos, SIGN, Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
            SignText text = new SignText();
            for (int i = 0; i < lines.size(); i++) {
                text = text.setMessage(i, component(lines.get(i)));
            }
            sign.setText(text, true);
            sign.setWaxed(true);
            sign.setChanged();
            level.sendBlockUpdated(pos, SIGN, SIGN, Block.UPDATE_CLIENTS);
        }
    }
}
