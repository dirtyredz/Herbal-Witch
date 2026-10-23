package io.github.dirtyredz.herbalwitch.magic.wand.action;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.hearth.*;
import io.github.dirtyredz.herbalwitch.apothecary.stations.block.StationBlock;
import io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode;
import io.github.dirtyredz.herbalwitch.magic.wand.use.*;
public final class LightAction {
 private LightAction(){}
 public static WandCommit prepare(ServerPlayer actor,BlockPos pos){
  var level=actor.serverLevel();var before=level.getBlockState(pos);if(!WandPermission.allowed(WandMode.LIGHT,actor,pos))return null;
  if(before.getBlock() instanceof StationBlock station){var origin=station.origin(before,pos);if(!level.hasChunkAt(origin)||!(level.getBlockEntity(origin) instanceof HearthBlockEntity hearth)||!HearthIgnition.ready(hearth)||!stationAllowed(actor,station,origin))return null;
   var saved=hearth.saveWithoutMetadata();return new WandCommit(){boolean changed;
    public boolean prepare(){return !hearth.isRemoved()&&hearth.saveWithoutMetadata().equals(saved)&&HearthIgnition.ready(hearth)&&WandPermission.allowed(WandMode.LIGHT,actor,pos)&&stationAllowed(actor,station,origin);}
    public boolean apply(){if(!prepare())return false;changed=HearthIgnition.ignite(hearth);if(changed)HearthIgnition.synchronize(hearth);return changed;}
    public void rollback(){if(changed){hearth.load(saved);HearthIgnition.synchronize(hearth);hearth.setChanged();}}
   };
  }
  if(!net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(before.getBlock()).getNamespace().equals("minecraft")||!(CandleBlock.canLight(before)||CandleCakeBlock.canLight(before)||CampfireBlock.canLight(before)))return null;
  return new WandCommit(){boolean changed;public boolean prepare(){return level.getBlockState(pos)==before&&WandPermission.allowed(WandMode.LIGHT,actor,pos);}public boolean apply(){if(!prepare())return false;changed=level.setBlock(pos,before.setValue(BlockStateProperties.LIT,true),Block.UPDATE_ALL);return changed;}public void rollback(){if(changed)level.setBlock(pos,before,Block.UPDATE_ALL);}public void finish(){level.gameEvent(actor,net.minecraft.world.level.gameevent.GameEvent.BLOCK_CHANGE,pos);}};
 }
 private static boolean stationAllowed(ServerPlayer actor,StationBlock station,BlockPos origin){
  var level=actor.serverLevel();var state=level.getBlockState(origin);var layout=station.layout();
  if(!state.is(station)||!station.isOrigin(state))return false;
  if(layout==null)return WandPermission.allowed(WandMode.LIGHT,actor,origin);
  var facing=state.getValue(StationBlock.FACING);
  for(int i=0;i<layout.size();i++){var pos=layout.cellPos(origin,facing,i);if(!level.hasChunkAt(pos))return false;var cell=level.getBlockState(pos);if(!cell.is(station)||cell.getValue(StationBlock.FACING)!=facing||cell.getValue(layout.part())!=i||!WandPermission.allowed(WandMode.LIGHT,actor,pos))return false;}
  return true;
 }
}
