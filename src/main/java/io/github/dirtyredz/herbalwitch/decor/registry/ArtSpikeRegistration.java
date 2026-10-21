package io.github.dirtyredz.herbalwitch.decor.registry;

import io.github.dirtyredz.herbalwitch.decor.block.HorizontalFurnitureBlock;
import io.github.dirtyredz.herbalwitch.decor.block.TableLampBlock;
import io.github.dirtyredz.herbalwitch.decor.block.WallShelfBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.ArtSpikeCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.Finish;
import io.github.dirtyredz.herbalwitch.decor.catalog.FinishCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.shape.ArtSpikeShapes;
import io.github.dirtyredz.herbalwitch.decor.showcase.DecorShowcaseSection;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Map;
import java.util.function.Function;

/**
 * Turns the art-spike catalogue into registered blocks and items: the five pieces, each in all 18
 * finishes. Block properties come from the piece's behaviour and the finish's appearance only, so
 * every finish of a piece is equally strong.
 */
public final class ArtSpikeRegistration {
    /** How each piece becomes a block, given finish-specific properties. */
    private static final Map<FurnitureSpec, Function<BlockBehaviour.Properties, Block>> FACTORIES = Map.of(
            ArtSpikeCatalog.ARMCHAIR, p -> new HorizontalFurnitureBlock(p, ArtSpikeShapes.ARMCHAIR),
            ArtSpikeCatalog.APOTHECARY_COUNTER, p -> new HorizontalFurnitureBlock(p, ArtSpikeShapes.APOTHECARY_COUNTER),
            ArtSpikeCatalog.SMALL_WALL_SHELF, p -> new WallShelfBlock(p, ArtSpikeShapes.SMALL_WALL_SHELF),
            ArtSpikeCatalog.TABLE_LAMP, p -> new TableLampBlock(p.lightLevel(TableLampBlock::lightEmission), ArtSpikeShapes.TABLE_LAMP),
            ArtSpikeCatalog.SMALL_RUG, p -> new CarpetBlock(p));

    private ArtSpikeRegistration() {
    }

    /** Called once from the mod constructor. */
    public static void register(IEventBus modBus) {
        for (FurnitureSpec spec : ArtSpikeCatalog.ALL) {
            var factory = FACTORIES.get(spec);
            for (Finish finish : FinishCatalog.ALL) {
                DecorBlocks.register(spec, finish, () -> factory.apply(properties(spec, finish)));
            }
        }
        DecorItems.registerBlockItems();
        DecorBlocks.BLOCKS.register(modBus);
        DecorItems.ITEMS.register(modBus);
        DecorCreativeTab.TABS.register(modBus);
        ShowcaseRegistry.register(new DecorShowcaseSection());
    }

    static BlockBehaviour.Properties properties(FurnitureSpec spec, Finish finish) {
        if (spec.behavior() == FurnitureSpec.Behavior.RUG) {
            // Vanilla carpet: breaks instantly, wool sounds.
            return BlockBehaviour.Properties.of().mapColor(finish.mapColor()).strength(0.1F).sound(SoundType.WOOL)
                    .ignitedByLava();
        }
        boolean shelfOrLamp = spec.behavior() != FurnitureSpec.Behavior.FREESTANDING;
        return BlockBehaviour.Properties.of()
                .mapColor(finish.mapColor())
                .instrument(NoteBlockInstrument.BASS)
                .strength(shelfOrLamp ? 1.0F : 2.0F, 3.0F)
                .sound(finish.frame() == Finish.FrameMaterial.CUT_STONE ? SoundType.STONE : SoundType.WOOD)
                .noOcclusion()
                .ignitedByLava();
    }
}
