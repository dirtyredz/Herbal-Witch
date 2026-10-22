package io.github.dirtyredz.herbalwitch.magic.ritual.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.ritual.altar.AltarBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarCatalog;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.CircleCatalog;
import io.github.dirtyredz.herbalwitch.magic.ritual.circle.RitualCircleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The functional altar ({@code magic/ritual/altar}) and the ritual circle ({@code magic/ritual/circle}): non-occluding, immovable by pistons. */
public final class RitualBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HerbalWitch.MOD_ID);

    public static final RegistryObject<Block> ALTAR = BLOCKS.register(AltarCatalog.PATH, () -> new AltarBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BROWN).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).noOcclusion()
            .pushReaction(PushReaction.BLOCK).lightLevel(state -> state.getValue(AltarBlock.ACTIVE) ? AltarCatalog.ACTIVE_LIGHT : 0)));

    public static final RegistryObject<Block> CIRCLE = BLOCKS.register(CircleCatalog.PATH, () -> new RitualCircleBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(1.5F, 6.0F).sound(SoundType.STONE).noOcclusion()
            .requiresCorrectToolForDrops().pushReaction(PushReaction.BLOCK)
            .lightLevel(state -> state.getValue(RitualCircleBlock.ACTIVE) ? CircleCatalog.ACTIVE_LIGHT : 0)));

    private RitualBlocks() {
    }
}
