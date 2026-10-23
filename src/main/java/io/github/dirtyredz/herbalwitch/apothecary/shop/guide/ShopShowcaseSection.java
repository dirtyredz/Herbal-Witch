package io.github.dirtyredz.herbalwitch.apothecary.shop.guide;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopDisplayBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopRegisterBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.registry.ShopBlocks;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.SurfacesCatalog;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseEntitySample;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement.Line;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Inert commerce demonstration (witchcraft group): closed and open registers on apothecary counters,
 * stocked and sold-out shelf and jar projections, an order board, notes on example offers, history and
 * request states, and one inert villager. Displays are visibly marked demonstrations that belong to no
 * shop: they cannot be linked, sell, transfer items, mint proceeds or enter villager demand.
 */
public final class ShopShowcaseSection implements ShowcaseSection {
    public static final String TITLE = "showcase." + HerbalWitch.MOD_ID + ".section." + ShopPieceCatalog.SHOWCASE;
    /** Exhibits front to back (one per row): closed register, open register, stocked and sold-out shelf, stocked and sold-out jar, board. */
    public static final List<String> EXHIBITS = List.of("register_closed", "register_open", "shelf_stocked", "shelf_sold_out", "jar_stocked", "jar_sold_out", "board");
    public static final List<String> NOTES = List.of("offers", "history", "orders", "villagers");
    public static final int SPACING = 3;
    /** Every exhibit block stands in this column, its sign one cell west; notes and the villager yard sit east. */
    public static final int COLUMN = 1;
    public static final int NOTES_X = 2;

    public static String label(String topic) {
        return "showcase." + HerbalWitch.MOD_ID + ".label.shop." + topic;
    }

    @Override
    public String id() {
        return ShopPieceCatalog.SHOWCASE;
    }

    @Override
    public String titleKey() {
        return TITLE;
    }

    /** The exhibit's block row (z) in section coordinates: a narrow four-wide section, deep rather than wide. */
    public static int row(int exhibit) {
        return 1 + exhibit * SPACING;
    }

    @Override
    public List<ShowcasePlacement> placements() {
        List<ShowcasePlacement> out = new ArrayList<>();
        Supplier<BlockState> counter = () -> DecorBlocks.block(SurfacesCatalog.APOTHECARY_COUNTER).defaultBlockState();
        Supplier<BlockState> wall = () -> Blocks.DARK_OAK_PLANKS.defaultBlockState();
        for (int i = 0; i < EXHIBITS.size(); i++) {
            String exhibit = EXHIBITS.get(i);
            int x = COLUMN, row = row(i);
            out.add(ShowcasePlacement.label(x - 1, row, Line.translated(label(exhibit)), Line.translated(label(exhibit) + ".detail")));
            switch (exhibit) {
                case "register_closed", "register_open" -> {
                    boolean open = exhibit.equals("register_open");
                    out.add(ShowcasePlacement.support(x, 1, row, counter));
                    out.add(ShowcasePlacement.content(x, 2, row, () -> ShopBlocks.REGISTER.get().defaultBlockState()
                            .setValue(ShopFixtureBlock.FACING, Direction.NORTH).setValue(ShopRegisterBlock.OPEN, open)));
                }
                case "shelf_stocked", "shelf_sold_out" -> {
                    boolean stocked = exhibit.equals("shelf_stocked");
                    out.add(ShowcasePlacement.support(x, 1, row + 1, wall));
                    out.add(ShowcasePlacement.support(x, 2, row + 1, wall));
                    out.add(ShowcasePlacement.content(x, 2, row, () -> ShopBlocks.STOCK_SHELF.get().defaultBlockState()
                            .setValue(ShopFixtureBlock.FACING, Direction.NORTH), demonstrate(() -> ShopShowcaseSamples.shelf(stocked))));
                }
                case "jar_stocked", "jar_sold_out" -> {
                    boolean stocked = exhibit.equals("jar_stocked");
                    out.add(ShowcasePlacement.support(x, 1, row, counter));
                    out.add(ShowcasePlacement.content(x, 2, row, () -> ShopBlocks.STOCK_JAR.get().defaultBlockState()
                            .setValue(ShopFixtureBlock.FACING, Direction.NORTH), demonstrate(() -> ShopShowcaseSamples.jar(stocked))));
                }
                default -> {
                    out.add(ShowcasePlacement.support(x, 1, row + 1, wall));
                    out.add(ShowcasePlacement.support(x, 2, row + 1, wall));
                    out.add(ShowcasePlacement.content(x, 2, row, () -> ShopBlocks.ORDER_BOARD.get().defaultBlockState()
                            .setValue(ShopFixtureBlock.FACING, Direction.NORTH)));
                }
            }
        }
        // A planked yard beside the jars where the inert villager sample stands.
        out.addAll(ShowcasePlacement.floorArea(NOTES_X, row(4), 2, 4, () -> Blocks.DARK_OAK_PLANKS.defaultBlockState()));
        for (int i = 0; i < NOTES.size(); i++) {
            String note = NOTES.get(i);
            out.add(ShowcasePlacement.label(NOTES_X, row(i), Line.translated(label(note)), Line.translated(label(note) + ".detail")));
        }
        return out;
    }

    /** One inert adult villager (no AI, no trades, no customer budget) in the yard beside the jars. */
    @Override
    public List<ShowcaseEntitySample> entities() {
        return List.of(new ShowcaseEntitySample("minecraft:villager", NOTES_X + 1.0, 1, row(4) + 2.0, true, 0));
    }

    private static Consumer<BlockEntity> demonstrate(Supplier<List<ShopDisplayBlockEntity.Sample>> samples) {
        return entity -> {
            if (entity instanceof ShopDisplayBlockEntity display) display.demonstrate(samples.get());
        };
    }

}
