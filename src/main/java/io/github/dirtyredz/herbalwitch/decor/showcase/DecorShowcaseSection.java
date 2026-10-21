package io.github.dirtyredz.herbalwitch.decor.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.block.TableLampBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.ArtSpikeCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.Finish;
import io.github.dirtyredz.herbalwitch.decor.catalog.FinishCatalog;
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
 * Every furniture piece in every finish: one row per piece, one column per finish, in catalogue
 * order. Each cell has a sign (finish, piece) in front, the piece facing the entrance (north), a
 * wall behind each shelf, and every lamp lit.
 */
public final class DecorShowcaseSection implements ShowcaseSection {
    public static final String TITLE = "showcase." + HerbalWitch.MOD_ID + ".section.decor";
    /** Row pitch: sign, piece, wall, aisle. */
    public static final int ROW = 4;
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
        for (int row = 0; row < pieces.size(); row++) {
            FurnitureSpec spec = pieces.get(row);
            for (int col = 0; col < FinishCatalog.ALL.size(); col++) {
                Finish finish = FinishCatalog.ALL.get(col);
                int x = col * COLUMN;
                int z = row * ROW;
                out.add(ShowcasePlacement.label(x, z, Line.literal(finish.displayName()), Line.literal(spec.displayName())));
                switch (spec.behavior()) {
                    case WALL_MOUNTED -> {
                        out.add(ShowcasePlacement.support(x, 1, z + 2, () -> Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState()));
                        out.add(ShowcasePlacement.support(x, 2, z + 2, () -> Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState()));
                        out.add(ShowcasePlacement.content(x, 2, z + 1, () -> facingNorth(spec, finish)));
                    }
                    case TABLE_LAMP -> out.add(ShowcasePlacement.content(x, 1, z + 1,
                            () -> DecorBlocks.block(spec, finish).defaultBlockState().setValue(TableLampBlock.LIT, true)));
                    case RUG -> out.add(ShowcasePlacement.content(x, 1, z + 1,
                            () -> DecorBlocks.block(spec, finish).defaultBlockState()));
                    case FREESTANDING -> out.add(ShowcasePlacement.content(x, 1, z + 1, () -> facingNorth(spec, finish)));
                }
            }
        }
        return out;
    }

    private static BlockState facingNorth(FurnitureSpec spec, Finish finish) {
        return DecorBlocks.block(spec, finish).defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
    }
}
