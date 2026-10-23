package io.github.dirtyredz.herbalwitch.magic.tools.restart;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import io.github.dirtyredz.herbalwitch.magic.broom.entity.BroomEntity;
import io.github.dirtyredz.herbalwitch.magic.broom.registry.*;
import io.github.dirtyredz.herbalwitch.magic.broom.persistence.BroomItemData;
import io.github.dirtyredz.herbalwitch.magic.broom.flight.BroomFlightState;
import io.github.dirtyredz.herbalwitch.magic.broom.network.*;
import io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode;
import io.github.dirtyredz.herbalwitch.magic.wand.persistence.WandItemData;
import io.github.dirtyredz.herbalwitch.magic.wand.registry.WandItems;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import io.github.dirtyredz.herbalwitch.apothecary.stations.block.*;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.authorization.StationOperations;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.execution.ProcessExecutionPolicy;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import java.nio.file.*;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
/** Two JVMs reopen actual saved world chunks; the evidence file only locates them and records expectations. */
@GameTestHolder("herbalwitch_tools_restart")
public final class MagicToolsRestartGameTests {
 private static final BlockPos BASE=new BlockPos(128,80,128),STAND=BASE.offset(4,0,0),CHEST=BASE.offset(0,0,4);
 private static Path evidence(){return Path.of(System.getProperty("herbalwitch.tools.restart.evidence"));}
 @GameTestGenerator public static Collection<TestFunction> restart(){String phase=System.getProperty("herbalwitch.tools.restart.phase","");if(phase.isEmpty())return List.of();if(!Set.of("prepare","verify").contains(phase))throw new IllegalArgumentException("Unknown tools restart phase");return List.of(new TestFunction("tools_restart","tools_restart."+phase,"herbalwitch_tools_restart:empty",600,0,true,h->{if(phase.equals("prepare"))prepare(h);else verify(h);}));}
 private static ItemStack broom(){var stack=new ItemStack(BroomItems.BROOM.get());new BroomItemData(350,23999,19).write(stack);stack.setHoverName(Component.literal("Restart Hazel"));return stack;}
 private static void prepare(GameTestHelper h){try{
  var level=h.getLevel();for(int x=-2;x<8;x++)for(int z=-2;z<8;z++){level.setBlock(BASE.offset(x,-1,z),Blocks.STONE.defaultBlockState(),Block.UPDATE_ALL);for(int y=0;y<5;y++)level.setBlock(BASE.offset(x,y,z),Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);}
  var actor=io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures.realPlayer(h,"tools_restart");actor.moveTo(BASE.getCenter());check(WitchStatusService.convert(actor,UUID.randomUUID()),"restart witch setup");
  var vehicle=BroomEntities.BROOM.get().create(level);vehicle.moveTo(BASE.getX()+.5,BASE.getY(),BASE.getZ()+.5,0,0);vehicle.initialize(actor.getUUID(),broom());check(level.addFreshEntity(vehicle)&&vehicle.mount(actor),"restart deploy/mount");var nonce=vehicle.controls().nonce();check(vehicle.input(actor,new BroomInput(nonce,0,BroomControlSession.UP)),"restart input");vehicle.flight(BroomFlightState.LANDING);
  level.setBlock(CHEST,Blocks.BARREL.defaultBlockState(),Block.UPDATE_ALL);var barrel=(BarrelBlockEntity)level.getBlockEntity(CHEST);barrel.setItem(0,broom());for(var mode:WandMode.values()){var wand=new ItemStack(WandItems.WAND.get());WandItemData.write(wand,mode);barrel.setItem(mode.ordinal()+1,wand);}barrel.setChanged();
  var block=(StationBlock)StationBlocks.BROOM_STAND.get();block.placeStation(level,STAND,block.defaultBlockState(),Block.UPDATE_ALL);var station=(ProcessStationEntity)level.getBlockEntity(STAND);station.inventory().setStackInSlot(0,new ItemStack(Items.WHEAT,2));station.inventory().setStackInSlot(1,new ItemStack(Items.STRING));station.inventory().setStackInSlot(4,broom());actor.stopRiding();actor.moveTo(STAND.getCenter().add(0,0,-2));station.serverTick();
  try(var online=OnlinePlayers.register(List.of(actor))){check(StationOperations.operate(actor,StationOperations.Operation.START,station.station(),station.magic(),new ProcessExecutionPolicy.Site(level,STAND)).equals("started"),"restart repair start");for(int i=0;i<30;i++)station.serverTick();}
  // Leave a genuine live input lease at the save boundary. No flight/wear advances after this point.
  actor.moveTo(vehicle.position().add(0,0,-1));check(vehicle.mount(actor),"restart final mount");vehicle.flight(BroomFlightState.LANDING);
  var meta=new CompoundTag();meta.putUUID("vehicle",vehicle.getUUID());meta.putUUID("owner",actor.getUUID());meta.putUUID("nonce",vehicle.controls().nonce());meta.put("packed",broom().save(new CompoundTag()));meta.putInt("energy",WitchStatusService.energyUnits(actor));meta.put("player",actor.saveWithoutId(new CompoundTag()));Files.createDirectories(evidence());NbtIo.writeCompressed(meta,evidence().resolve("expected.nbt").toFile());
  level.getServer().saveEverything(true,true,true);report("prepared",List.of("live_lease","interrupted_repair","packed_tools"));h.succeed();
 }catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}}
 private static void verify(GameTestHelper h){try{
  var meta=NbtIo.readCompressed(evidence().resolve("expected.nbt").toFile());h.getLevel().getChunkAt(BASE);h.getLevel().getChunkAt(STAND);h.getLevel().getChunkAt(CHEST);h.runAfterDelay(10,()->loaded(h,meta,200));
 }catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}}
 private static void loaded(GameTestHelper h,CompoundTag meta,int retries){var level=h.getLevel();if(!(level.getEntity(meta.getUUID("vehicle")) instanceof BroomEntity vehicle)){if(retries>0){h.runAfterDelay(1,()->loaded(h,meta,retries-1));return;}check(false,"saved world broom missing");return;}
  check(vehicle.owner().equals(meta.getUUID("owner"))&&ItemStack.matches(vehicle.packed(),ItemStack.of(meta.getCompound("packed"))),"deployed condition/name changed");check(vehicle.controls()==null&&!vehicle.isVehicle()&&vehicle.flight()!=BroomFlightState.FLYING,"restart retained control lease");
  var barrel=(BarrelBlockEntity)level.getBlockEntity(CHEST);check(barrel!=null&&ItemStack.matches(barrel.getItem(0),ItemStack.of(meta.getCompound("packed"))),"packed metadata lost");for(var mode:WandMode.values())check(WandItemData.read(barrel.getItem(mode.ordinal()+1)).orElseThrow()==mode,"wand preference lost");
  var actor=io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures.realPlayer(h,"tools_restart");actor.load(meta.getCompound("player"));actor.moveTo(STAND.getCenter().add(0,0,-2));var station=(ProcessStationEntity)level.getBlockEntity(STAND);check(station!=null,"saved station missing");int energy=WitchStatusService.energyUnits(actor);check(energy==meta.getInt("energy"),"offline energy changed");
  try(var online=OnlinePlayers.register(List.of(actor))){for(int i=0;i<150;i++)station.serverTick();check(station.inventory().getStackInSlot(3).isEmpty()&&ItemStack.matches(station.inventory().getStackInSlot(4),ItemStack.of(meta.getCompound("packed"))),"interrupted repair resumed itself");check(WitchStatusService.energyUnits(actor)==energy,"offline repair charged");check(StationOperations.operate(actor,StationOperations.Operation.CANCEL,station.station(),station.magic(),new ProcessExecutionPolicy.Site(level,STAND)).equals("cancelled"),"restored repair cannot cancel");check(station.inventory().extractItem(4,1,false).getCount()==1,"cancel lost broom or reservation");}
  check(!vehicle.input(actor,new BroomInput(meta.getUUID("nonce"),1,BroomControlSession.UP)),"old lease replay accepted");check(level.getEntitiesOfClass(BroomEntity.class,new net.minecraft.world.phys.AABB(BASE.offset(-8,-2,-8),BASE.offset(16,8,16))).size()==1,"deployed broom duplicated");report("verified",List.of("world_entity","no_lease","packed_metadata","seven_modes","repair_paused","cancel_conserves","no_offline_spend","old_nonce_refused","one_broom"));h.succeed();
 }
 private static void report(String phase,List<String> checks){try{Files.writeString(evidence().resolve(phase+".json"),new com.google.gson.Gson().toJson(Map.of("process",ProcessHandle.current().pid(),"checks",checks)));}catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}}
}
