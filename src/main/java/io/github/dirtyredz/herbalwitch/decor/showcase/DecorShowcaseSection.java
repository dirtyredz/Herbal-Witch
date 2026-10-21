package io.github.dirtyredz.herbalwitch.decor.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.block.TableLampBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.ArtSpikeCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement.Line;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Every furniture piece, once, in catalogue order along one row: a sign in front, the piece facing
 * the entrance (north), a wall behind the shelf, and the lamp lit.
 */
public final class DecorShowcaseSection implements ShowcaseSection {
    public static final String TITLE = "showcase." + HerbalWitch.MOD_ID + ".section.decor";
    public static final int COLUMN = 2;

    @Override
    public String id() {
        return "decor";
    }

    @Override
    public String titleKey() {
        return TITLE;
    }

    @Override
    public List<ShowcasePlacement> placements() {
        List<ShowcasePlacement> out = new ArrayList<>();
        List<FurnitureSpec> pieces = ArtSpikeCatalog.ALL;
        for (int i = 0; i < pieces.size(); i++) {
            FurnitureSpec spec = pieces.get(i);
            int x = i * COLUMN;
            out.add(ShowcasePlacement.label(x, 0, Line.literal(spec.displayName())));
            switch (spec.behavior()) {
                case WALL_MOUNTED -> {
                    out.add(ShowcasePlacement.support(x, 1, 2, () -> Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState()));
                    out.add(ShowcasePlacement.support(x, 2, 2, () -> Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState()));
                    out.add(ShowcasePlacement.content(x, 2, 1, () -> facingNorth(spec)));
                }
                case TABLE_LAMP -> out.add(ShowcasePlacement.content(x, 1, 1,
                        () -> DecorBlocks.block(spec).defaultBlockState().setValue(TableLampBlock.LIT, true)));
                case RUG -> out.add(ShowcasePlacement.content(x, 1, 1, () -> DecorBlocks.block(spec).defaultBlockState()));
                case FREESTANDING -> out.add(ShowcasePlacement.content(x, 1, 1, () -> facingNorth(spec)));
            }
        }
        return out;
    }

    private static BlockState facingNorth(FurnitureSpec spec) {
        return DecorBlocks.block(spec).defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
    }
}
