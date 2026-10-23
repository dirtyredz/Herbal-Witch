package io.github.dirtyredz.herbalwitch.apothecary.botany.harvest;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.sounds.*;
import net.minecraft.world.level.gameevent.GameEvent;
import io.github.dirtyredz.herbalwitch.platform.permission.*;
/** Synchronous reversible crop/soil changes; yield and sound are emitted only after commit. */
public final class HerbHarvestCommit {
 private final ServerLevel level;private final Player actor;private final HerbHarvestPlan plan;private boolean changed,emitted;
 public HerbHarvestCommit(ServerLevel level,Player actor,HerbHarvestPlan plan){this.level=level;this.actor=actor;this.plan=plan;}
 public boolean ready(){return !changed&&level.hasChunkAt(plan.pos())&&level.getBlockState(plan.pos())==plan.before()&&level.getBlockState(plan.pos().below())==plan.soil()&&ActionPermissionService.mayModify(ActionContext.of(actor,level,plan.pos()))&&(plan.soil()==plan.nextSoil()||ActionPermissionService.mayModify(ActionContext.of(actor,level,plan.pos().below())));}
 public boolean apply(){if(!ready()||!level.setBlock(plan.pos(),plan.after(),Block.UPDATE_CLIENTS))return false;changed=true;return plan.soil()==plan.nextSoil()||level.setBlock(plan.pos().below(),plan.nextSoil(),Block.UPDATE_CLIENTS);}
 public void rollback(){if(!changed||emitted)return;level.setBlock(plan.pos(),plan.before(),Block.UPDATE_CLIENTS);if(plan.soil()!=plan.nextSoil())level.setBlock(plan.pos().below(),plan.soil(),Block.UPDATE_CLIENTS);changed=false;}
 public void emit(){if(!changed||emitted)return;emitted=true;for(var stack:plan.drops())Block.popResource(level,plan.pos(),stack.copy());level.playSound(null,plan.pos(),SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,SoundSource.BLOCKS,1F,.8F+level.random.nextFloat()*.4F);level.gameEvent(actor,GameEvent.BLOCK_CHANGE,plan.pos());}
}
