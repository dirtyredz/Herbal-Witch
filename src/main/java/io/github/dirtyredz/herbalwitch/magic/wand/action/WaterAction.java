package io.github.dirtyredz.herbalwitch.magic.wand.action;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import io.github.dirtyredz.herbalwitch.apothecary.botany.garden.HerbBedBlock;
import io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode;
import io.github.dirtyredz.herbalwitch.magic.wand.use.*;
/** Moisture alone: preserve enrichment, connections, crops and every other property. */
public final class WaterAction {
 private WaterAction(){}
 public static WandCommit prepare(ServerPlayer actor,BlockPos pos){
  var level=actor.serverLevel();var before=level.getBlockState(pos);
  if(!(before.is(Blocks.FARMLAND)||before.getBlock() instanceof HerbBedBlock)||before.getValue(BlockStateProperties.MOISTURE)==7||!WandPermission.allowed(WandMode.WATER,actor,pos))return null;
  return new WandCommit(){boolean changed;public boolean prepare(){return level.getBlockState(pos)==before&&WandPermission.allowed(WandMode.WATER,actor,pos);}public boolean apply(){if(!prepare())return false;changed=level.setBlock(pos,before.setValue(BlockStateProperties.MOISTURE,7),Block.UPDATE_CLIENTS);return changed;}public void rollback(){if(changed)level.setBlock(pos,before,Block.UPDATE_CLIENTS);}};
 }
}
