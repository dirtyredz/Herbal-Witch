package io.github.dirtyredz.herbalwitch.magic.broom.interaction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
import io.github.dirtyredz.herbalwitch.magic.broom.registry.BroomEntities;
import io.github.dirtyredz.herbalwitch.magic.broom.persistence.BroomItemData;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
/** Checked physical deployment: reject before insertion, consume only after insertion succeeds. */
public final class BroomDeployment {
 private BroomDeployment(){}
 public static InteractionResult use(UseOnContext context){
  if(context.getLevel().isClientSide)return InteractionResult.SUCCESS;
  if(!(context.getPlayer() instanceof ServerPlayer actor)||!WitchStatusService.isWitch(actor)||actor.isPassenger()||BroomItemData.read(context.getItemInHand()).isEmpty())return InteractionResult.FAIL;
  var level=(ServerLevel)context.getLevel();var at=context.getClickedPos().relative(context.getClickedFace());
  if(!BroomAccess.allowed(BroomAccess.Action.DEPLOY,actor,level,at)||actor.getEyePosition().distanceToSqr(Vec3.atCenterOf(at))>25)return InteractionResult.FAIL;
  var broom=BroomEntities.BROOM.get().create(level);if(broom==null)return InteractionResult.FAIL;
  broom.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,actor.getYRot(),0);broom.initialize(actor.getUUID(),context.getItemInHand());
  if(!io.github.dirtyredz.herbalwitch.magic.broom.flight.BroomFlightController.loaded(broom,broom.getBoundingBox())||!level.noCollision(broom)||!level.getEntities(broom,broom.getBoundingBox(),e->!e.isSpectator()).isEmpty())return InteractionResult.FAIL;
  if(!level.addFreshEntity(broom))return InteractionResult.FAIL;
  if(!actor.getAbilities().instabuild)context.getItemInHand().shrink(1);return InteractionResult.CONSUME;
 }
}
