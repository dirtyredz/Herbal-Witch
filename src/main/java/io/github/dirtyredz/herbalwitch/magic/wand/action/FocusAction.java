package io.github.dirtyredz.herbalwitch.magic.wand.action;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import io.github.dirtyredz.herbalwitch.magic.ritual.altar.AltarBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.circle.RitualCircleBlock;
import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlock;
/** Existing block/menu entry points retain complete-structure, reach, consent and ownership checks. */
public final class FocusAction {
 private FocusAction(){}
 public static boolean open(ServerPlayer actor,InteractionHand hand,BlockHitResult hit){
  var state=actor.level().getBlockState(hit.getBlockPos());if(!(state.getBlock() instanceof AltarBlock||state.getBlock() instanceof RitualCircleBlock||state.getBlock() instanceof WardStoneBlock))return false;
  return state.use(actor.level(),actor,hand,hit).consumesAction();
 }
}
