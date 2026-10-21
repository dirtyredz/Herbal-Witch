package io.github.dirtyredz.herbalwitch.brewing.product.lighting;

import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** The lantern's remaining oil in loaded ticks; it goes out at zero. Saved with the chunk. */
public class OilLanternBlockEntity extends BlockEntity {
    /** One bottle: five minutes of loaded time. */
    public static final int FUEL_TICKS = 6_000;

    private int fuel;

    public OilLanternBlockEntity(BlockPos pos, BlockState state) {
        super(PreparationBlockEntities.OIL_LANTERN.get(), pos, state);
    }

    public int fuel() {
        return fuel;
    }

    public void refuel() {
        fuel = FUEL_TICKS;
        setChanged();
    }

    /** Sets the remaining fuel directly (showcase samples and tests). */
    public void setFuel(int ticks) {
        fuel = Math.max(0, Math.min(FUEL_TICKS, ticks));
        setChanged();
    }

    void serverTick() {
        if (fuel > 0) {
            fuel--;
            setChanged();
        }
        BlockState state = getBlockState();
        if (fuel == 0 && state.getValue(OilLanternBlock.LIT)) {
            level.setBlock(worldPosition, state.setValue(OilLanternBlock.LIT, false), Block.UPDATE_ALL);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fuel = tag.getInt("Fuel");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Fuel", fuel);
    }
}
