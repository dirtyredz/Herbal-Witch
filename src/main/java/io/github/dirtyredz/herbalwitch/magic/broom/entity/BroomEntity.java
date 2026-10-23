package io.github.dirtyredz.herbalwitch.magic.broom.entity;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import io.github.dirtyredz.herbalwitch.magic.broom.flight.*;
import io.github.dirtyredz.herbalwitch.magic.broom.interaction.*;
import io.github.dirtyredz.herbalwitch.magic.broom.lifecycle.BroomDismount;
import io.github.dirtyredz.herbalwitch.magic.broom.network.*;
import io.github.dirtyredz.herbalwitch.magic.broom.persistence.*;
/** Persistent physical broom. Client vehicle-position authority is deliberately never assigned. */
public final class BroomEntity extends Entity {
 private static final EntityDataAccessor<Integer> STATE=SynchedEntityData.defineId(BroomEntity.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Integer> CONDITION=SynchedEntityData.defineId(BroomEntity.class,EntityDataSerializers.INT);
 private UUID owner;
 private ItemStack item=ItemStack.EMPTY;
 private CompoundTag unreadable;
 private BroomControlSession controls;
 private boolean flown;
 private Vec3 lerpTarget=Vec3.ZERO;
 private int lerpSteps;
 private float lerpYaw;
 public BroomEntity(EntityType<? extends BroomEntity> type,Level level){super(type,level);setNoGravity(true);}
 @Override protected void defineSynchedData(){entityData.define(STATE,BroomFlightState.LANDING.ordinal());entityData.define(CONDITION,0);}
 public UUID owner(){return owner;}
 public ItemStack packed(){return item.copy();}
 public boolean writable(){return unreadable==null && owner!=null && BroomItemData.read(item).isPresent();}
 public BroomItemData condition(){return BroomItemData.read(item).orElseThrow();}
 public int remaining(){return entityData.get(CONDITION);}
 public void condition(BroomItemData value){value.write(item);entityData.set(CONDITION,value.condition());}
 public BroomFlightState flight(){return BroomFlightState.values()[entityData.get(STATE)];}
 public void flight(BroomFlightState value){entityData.set(STATE,value.ordinal());}
 public void initialize(UUID owner,ItemStack stack){this.owner=owner;item=stack.copyWithCount(1);condition(condition());flight(BroomFlightState.GROUNDED);}
 public BroomControlSession controls(){return controls;}
 public void flew(){flown=true;}
 @Nullable public ServerPlayer rider(){return getFirstPassenger() instanceof ServerPlayer p?p:null;}
 public boolean mount(ServerPlayer actor){
  if(!BroomAccess.mount(actor,this)||!actor.startRiding(this))return false;
  controls=new BroomControlSession(actor.getUUID(),UUID.randomUUID(),level().getGameTime());flown=false;
  BroomPackets.lease(actor,this,controls.nonce());return true;
 }
 public boolean input(ServerPlayer actor,BroomInput input){return rider()==actor && controls!=null && controls.accept(actor.getUUID(),input.nonce(),input.sequence(),input.flags(),level().getGameTime());}
 public void endFlight(){controls=null;setDeltaMovement(Vec3.ZERO);flight(BroomFlightState.LANDING);}
 @Override protected boolean canAddPassenger(Entity passenger){return !isVehicle()&&passenger instanceof Player&&passenger.getUUID().equals(owner);}
 /** Opt out of vanilla's player-owned mount save/removal path; this entity always belongs to its chunk. */
 @Override public boolean hasExactlyOnePlayerPassenger(){return false;}
 @Override public LivingEntity getControllingPassenger(){return null;}
 @Override public boolean isControlledByLocalInstance(){return false;}
 @Override public boolean canChangeDimensions(){return false;}
 @Override public boolean isPickable(){return !isRemoved();}
 @Override public boolean isPushable(){return false;}
 @Override public double getPassengersRidingOffset(){return io.github.dirtyredz.herbalwitch.magic.broom.catalog.BroomSpec.RIDER_OFFSET;}
 @Override protected void removePassenger(Entity passenger){super.removePassenger(passenger);if(!level().isClientSide){if(flown&&passenger instanceof ServerPlayer p)BroomDismount.protect(p);endFlight();flown=false;}}
 @Override public Vec3 getDismountLocationForPassenger(LivingEntity passenger){return BroomDismount.location(this,passenger);}
 @Override public InteractionResult interact(Player player,InteractionHand hand){
  if(level().isClientSide)return InteractionResult.SUCCESS;
  if(!(player instanceof ServerPlayer actor))return InteractionResult.FAIL;
  boolean success=actor.isShiftKeyDown()?BroomPacking.pack(actor,this):mount(actor);
  if(!success)actor.displayClientMessage(net.minecraft.network.chat.Component.translatable("broom.herbalwitch.refused"),true);
  return success?InteractionResult.CONSUME:InteractionResult.FAIL;
 }
 @Override public void tick(){
  super.tick();
  if(level().isClientSide){if(lerpSteps>0){setPos(position().lerp(lerpTarget,1.0/lerpSteps));setYRot((float)net.minecraft.util.Mth.rotLerp(1F/lerpSteps,getYRot(),lerpYaw));lerpSteps--;}return;}
  if(writable())BroomFlightController.tick(this);
 }
 @Override public void lerpTo(double x,double y,double z,float yaw,float pitch,int steps,boolean teleport){lerpTarget=new Vec3(x,y,z);lerpYaw=yaw;lerpSteps=Math.max(1,Math.min(steps,3));}
 @Override protected void addAdditionalSaveData(CompoundTag tag){if(unreadable!=null)tag.put("Broom",unreadable.copy());else if(owner!=null)tag.put("Broom",BroomEntityCodec.encode(owner,item));}
 @Override protected void readAdditionalSaveData(CompoundTag tag){
  CompoundTag raw=tag.getCompound("Broom");var saved=BroomEntityCodec.decode(raw);
  controls=null;flown=false;setDeltaMovement(Vec3.ZERO);flight(BroomFlightState.LANDING);
  if(saved.isEmpty()){unreadable=raw.copy();owner=null;item=ItemStack.EMPTY;return;}
  unreadable=null;owner=saved.get().owner();item=saved.get().item().copy();condition(condition());
 }
 @Override public void remove(RemovalReason reason){if(!level().isClientSide){ejectPassengers();endFlight();}super.remove(reason);}
 @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
