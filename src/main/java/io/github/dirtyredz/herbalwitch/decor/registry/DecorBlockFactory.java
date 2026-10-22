package io.github.dirtyredz.herbalwitch.decor.registry;

import io.github.dirtyredz.herbalwitch.decor.structure.DecorAssemblyBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.FinishCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.behavior.lighting.DecorLightBlock;
import io.github.dirtyredz.herbalwitch.decor.behavior.openings.DecorDoorBlock;
import io.github.dirtyredz.herbalwitch.decor.behavior.sleeping.DecorBedBlock;
import io.github.dirtyredz.herbalwitch.decor.behavior.seating.SeatingBlock;
import io.github.dirtyredz.herbalwitch.decor.behavior.storage.StorageFurnitureBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;

/**
 * The one place that knows which block class a piece uses: one constructor per behaviour, never
 * one per piece. Properties follow the material: wood furniture (axe, wood sounds, burns), textiles
 * (wool, instant break), and every multiblock refuses pistons.
 */
public final class DecorBlockFactory {
    private DecorBlockFactory() {
    }

    public static Block create(FurnitureSpec spec) {
        BlockBehaviour.Properties properties = properties(spec);
        return switch (spec.behavior()) {
            case SEATING -> new SeatingBlock(spec, properties);
            case STORAGE, DISPLAY -> new StorageFurnitureBlock(spec, properties);
            case LIGHT -> new DecorLightBlock(spec, properties);
            case BED -> new DecorBedBlock(spec, properties);
            case DOOR -> new DecorDoorBlock(spec, properties);
            case RUG -> spec.multiblock() ? new DecorAssemblyBlock(spec, properties) : new CarpetBlock(properties);
            default -> new DecorAssemblyBlock(spec, properties);
        };
    }

    static BlockBehaviour.Properties properties(FurnitureSpec spec) {
        BlockBehaviour.Properties p = BlockBehaviour.Properties.of().mapColor(FinishCatalog.BASE.mapColor()).noOcclusion().ignitedByLava();
        if (spec.behavior() == FurnitureBehavior.RUG) {
            p = p.strength(0.1F).sound(SoundType.WOOL); // vanilla carpet
        } else {
            // one-block shelves and lamps break faster than furniture (0.1.0 hardness)
            boolean slight = !spec.multiblock() && (spec.behavior() == FurnitureBehavior.DISPLAY || spec.behavior() == FurnitureBehavior.LIGHT);
            p = p.instrument(NoteBlockInstrument.BASS).strength(slight ? 1.0F : 2.0F, 3.0F).sound(SoundType.WOOD);
        }
        if (spec.multiblock()) {
            p = p.pushReaction(PushReaction.BLOCK);
        }
        return p;
    }
}
