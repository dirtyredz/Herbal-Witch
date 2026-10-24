package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.lighting.OilLanternBlock;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.lighting.OilLanternBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement.Line;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;

import java.util.ArrayList;
import java.util.List;

/**
 * Every preparation: the fourteen dried bundles hanging on a wall (each labelled), a barrel of every
 * powder, a barrel of every finished product, and a lit oil lantern.
 */
public final class PreparationShowcaseSection implements ShowcaseSection {
    public static final String TITLE = "showcase." + HerbalWitch.MOD_ID + ".section.preparations";
    public static final String POWDERS = "showcase." + HerbalWitch.MOD_ID + ".label.powders";
    public static final String PRODUCTS = "showcase." + HerbalWitch.MOD_ID + ".label.products";
    public static final String LANTERN = "showcase." + HerbalWitch.MOD_ID + ".label.oil_lantern";
    /** Columns per sample: the block, one clear cell and its east-side sign. */
    private static final int STRIDE = 3;
    /** Fold the long bundle row so the wider labels still fit the combined showcase budget. */
    private static final int COLUMNS = 3;

    @Override
    public String id() {
        return "preparations";
    }

    @Override
    public String titleKey() {
        return TITLE;
    }

    @Override
    public List<ShowcasePlacement> placements() {
        List<ShowcasePlacement> out = new ArrayList<>();
        for (int i = 0; i < HerbCatalog.ALL.size(); i++) {
            HerbSpec herb = HerbCatalog.ALL.get(i);
            int x = STRIDE * (i % COLUMNS);
            int z = 1 + (i / COLUMNS) * 4;
            out.add(ShowcasePlacement.sampleLabel(x + 1, z, Line.literal(herb.displayName()), Line.literal("Bundle")));
            out.add(ShowcasePlacement.support(x + 1, 1, z + 1, () -> Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState()));
            out.add(ShowcasePlacement.support(x + 1, 2, z + 1, () -> Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState()));
            out.add(ShowcasePlacement.content(x + 1, 2, z, () -> PreparationBlocks.bundle(herb).defaultBlockState()
                    .setValue(FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.WALL)
                    .setValue(FaceAttachedHorizontalDirectionalBlock.FACING, Direction.NORTH)));
        }
        int x = 0;
        int z = 1 + ((HerbCatalog.ALL.size() + COLUMNS - 1) / COLUMNS) * 4;
        out.add(ShowcasePlacement.sampleLabel(x + 1, z, Line.translated(POWDERS)));
        out.add(ShowcasePlacement.content(x + 1, 1, z, () -> Blocks.BARREL.defaultBlockState(), entity -> {
            if (entity instanceof Container barrel) {
                for (int i = 0; i < HerbCatalog.ALL.size(); i++) {
                    barrel.setItem(i, new ItemStack(PreparationItems.powder(HerbCatalog.ALL.get(i)), 8));
                }
            }
        }));
        out.add(ShowcasePlacement.sampleLabel(x + STRIDE + 1, z, Line.translated(PRODUCTS)));
        out.add(ShowcasePlacement.content(x + STRIDE + 1, 1, z, () -> Blocks.BARREL.defaultBlockState(), entity -> {
            if (entity instanceof Container barrel) {
                for (int i = 0; i < EverydayProductCatalog.ALL.size(); i++) {
                    barrel.setItem(i, new ItemStack(PreparationItems.product(EverydayProductCatalog.ALL.get(i)), 4));
                }
            }
        }));
        out.add(ShowcasePlacement.sampleLabel(x + 2 * STRIDE + 1, z, Line.translated(LANTERN)));
        out.add(ShowcasePlacement.content(x + 2 * STRIDE + 1, 1, z,
                () -> PreparationBlocks.OIL_LANTERN.get().defaultBlockState().setValue(OilLanternBlock.LIT, true), entity -> {
                    if (entity instanceof OilLanternBlockEntity lantern) {
                        lantern.setFuel(OilLanternBlockEntity.FUEL_TICKS);
                    }
                }));
        return out;
    }
}
