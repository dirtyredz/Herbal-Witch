package io.github.dirtyredz.herbalwitch.decor.behavior.seating;

import io.github.dirtyredz.herbalwitch.decor.registry.DecorEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The invisible, stationary thing a sitter rides: one per occupied seat, created by the server when
 * someone sits and gone as soon as nobody does. It is never saved (a world never holds an orphan
 * seat), holds no chunk ticket, has no physics, and removes itself when its sitter leaves or its
 * furniture is no longer there. The client only renders nothing at its position.
 */
public class SeatEntity extends Entity {
    private BlockPos origin = BlockPos.ZERO;
    private int seat;

    public SeatEntity(EntityType<? extends SeatEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        setNoGravity(true);
        setInvisible(true);
    }

    /** A seat at {@code pos} for seat {@code seat} of the structure whose origin is {@code origin}. */
    public static SeatEntity at(Level level, Vec3 pos, BlockPos origin, int seat) {
        SeatEntity entity = new SeatEntity(DecorEntities.SEAT.get(), level);
        entity.setPos(pos.x, pos.y, pos.z);
        entity.origin = origin.immutable();
        entity.seat = seat;
        return entity;
    }

    public BlockPos origin() {
        return origin;
    }

    public int seat() {
        return seat;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && (getPassengers().isEmpty() || !SeatingService.stillSeated(this))) {
            ejectPassengers();
            discard();
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!level().isClientSide && getPassengers().isEmpty()) {
            discard();
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 exit = SeatingService.exit(this, passenger);
        return exit != null ? exit : super.getDismountLocationForPassenger(passenger);
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.0;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty();
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
