package io.github.dirtyredz.herbalwitch.grimoire.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.grimoire.access.block.ResearchAccessBlock;
import io.github.dirtyredz.herbalwitch.grimoire.access.block.ResearchShapes;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.GrimoireCatalog;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.GrimoireCatalog.ResearchPiece;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The research blocks, ids {@code grimoire/<piece>}: wooden, non-occluding, axe-mined. */
public final class GrimoireBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HerbalWitch.MOD_ID);
    private static final Map<ResearchPiece, RegistryObject<Block>> BY_PIECE = new LinkedHashMap<>();

    public static final RegistryObject<Block> STAND = register(GrimoireCatalog.STAND, ResearchShapes.STAND);
    public static final RegistryObject<Block> BOOKSHELF = register(GrimoireCatalog.BOOKSHELF, ResearchShapes.BOOKSHELF);

    private GrimoireBlocks() {
    }

    private static RegistryObject<Block> register(ResearchPiece piece, Map<Direction, VoxelShape> shapes) {
        RegistryObject<Block> block = BLOCKS.register(piece.path(), () -> new ResearchAccessBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD)
                .noOcclusion().ignitedByLava(), piece, shapes));
        BY_PIECE.put(piece, block);
        return block;
    }

    public static Block block(ResearchPiece piece) {
        return BY_PIECE.get(piece).get();
    }

    /** Catalogue order. */
    public static List<Block> all() {
        return GrimoireCatalog.RESEARCH_PIECES.stream().map(GrimoireBlocks::block).toList();
    }
}
