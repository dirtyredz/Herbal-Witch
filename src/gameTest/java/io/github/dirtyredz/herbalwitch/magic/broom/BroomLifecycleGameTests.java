package io.github.dirtyredz.herbalwitch.magic.broom;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.broom.registry.BroomEntities;
import io.github.dirtyredz.herbalwitch.magic.broom.flight.BroomFlightState;
import io.github.dirtyredz.herbalwitch.magic.broom.lifecycle.*;
import io.github.dirtyredz.herbalwitch.magic.broom.persistence.BroomItemData;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.gametest.*;
import static io.github.dirtyredz.herbalwitch.magic.broom.fixture.BroomFixtures.*;
import static io.github.dirtyredz.herbalwitch.magic.broom.fixture.BroomFixtures.witch;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
@GameTestHolder(HerbalWitch.MOD_ID) @PrefixGameTestTemplate(false)
public final class BroomLifecycleGameTests {
 @GameTest(template=ARENA) public static void reloadPreservesPhysicalItemButNeverTheInputLease(GameTestHelper h){
  floor(h);var actor=witch(h,"broom_reload",AT.offset(0,0,-2));var b=broom(h,actor);b.condition(new BroomItemData(350,23999,19));up(actor,b);for(int i=0;i<5;i++)b.tick();var expected=b.condition();var tag=b.saveWithoutId(new CompoundTag());actor.stopRiding();b.discard();var restored=BroomEntities.BROOM.get().create(h.getLevel());restored.load(tag);check(restored.owner().equals(actor.getUUID())&&restored.condition().equals(expected),"physical data changed");check(restored.controls()==null&&!restored.isVehicle()&&restored.getDeltaMovement().lengthSqr()==0&&restored.flight()==BroomFlightState.LANDING,"resumed lease or motion");
  var corrupt=tag.copy();corrupt.getCompound("Broom").putInt("schema",99);restored.load(corrupt);check(!restored.writable()&&!restored.mount(actor),"unknown entity writable");check(restored.saveWithoutId(new CompoundTag()).getCompound("Broom").equals(corrupt.getCompound("Broom")),"unknown schema rewritten");h.succeed();
 }
 @GameTest(template=ARENA) public static void logoutAndDimensionTravelLeaveOneBroomWithoutAControlLease(GameTestHelper h){
  floor(h);var actor=witch(h,"broom_logout",AT.offset(0,0,-2));var b=broom(h,actor);up(actor,b);b.tick();net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(actor));check(!actor.isPassenger()&&!b.isRemoved()&&b.controls()==null,"logout retained passenger or lost broom");
  for(int i=0;i<10;i++)b.tick();actor.moveTo(b.position().add(0,0,-1));up(actor,b);var event=new net.minecraftforge.event.entity.EntityTravelToDimensionEvent(actor,net.minecraft.world.level.Level.NETHER);BroomLifecycle.dimension(event);check(!actor.isPassenger()&&!b.isRemoved()&&b.level()==h.getLevel()&&b.controls()==null,"dimension moved broom");h.succeed();
 }
 @GameTest(template=ARENA) public static void forgedVanillaVehiclePositionsNeverMoveTheServerBroom(GameTestHelper h){
  floor(h);var actor=witch(h,"broom_protocol",AT.offset(0,0,-2));var b=broom(h,actor);up(actor,b);
  var server=h.getLevel().getServer();boolean flight=server.isFlightAllowed();var previous=actor.connection;
  try{server.setFlightAllowed(false);var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);var listener=new net.minecraft.server.network.ServerGamePacketListenerImpl(server,connection,actor);actor.connection=listener;
   listener.tick();var before=b.position();var forged=BroomEntities.BROOM.get().create(h.getLevel());forged.moveTo(before.add(100,50,100));var wire=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());try{new net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket(forged).write(wire);listener.handleMoveVehicle(new net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket(wire));}finally{wire.release();}check(b.position().equals(before),"vanilla packet moved authoritative entity");
   for(int i=0;i<90;i++){b.tick();listener.tick();}check(actor.isPassenger()&&!b.isRemoved(),"default flight rules ended valid riding");
  }finally{actor.connection=previous;server.setFlightAllowed(flight);actor.stopRiding();}h.succeed();
 }
 @GameTest(template=ARENA) public static void fallGraceRequiresActualFlightAndProtectsOnlyOneOrdinaryFall(GameTestHelper h){
  floor(h);var actor=witch(h,"broom_fall",AT.offset(0,0,-2));var b=broom(h,actor);check(b.mount(actor),"mount");actor.stopRiding();var ground=new net.minecraftforge.event.entity.living.LivingFallEvent(actor,20,1);BroomDismount.fall(ground);check(!ground.isCanceled(),"grounded loop granted grace");for(int i=0;i<2;i++)b.tick();up(actor,b);for(int i=0;i<5;i++)b.tick();actor.stopRiding();var fall=new net.minecraftforge.event.entity.living.LivingFallEvent(actor,20,1);BroomDismount.fall(fall);check(fall.isCanceled(),"real flight had no fall grace");var repeat=new net.minecraftforge.event.entity.living.LivingFallEvent(actor,20,1);BroomDismount.fall(repeat);check(!repeat.isCanceled(),"grace reusable");h.succeed();
 }
 @GameTest(template=ARENA) public static void aBlockedTakeoffCannotFarmFallProtection(GameTestHelper h){
  floor(h);h.setBlock(AT.above(2),net.minecraft.world.level.block.Blocks.STONE);var actor=witch(h,"broom_blocked_grace",AT.offset(0,0,-2));var b=broom(h,actor);up(actor,b);for(int i=0;i<20;i++)b.tick();actor.stopRiding();var fall=new net.minecraftforge.event.entity.living.LivingFallEvent(actor,20,1);BroomDismount.fall(fall);check(!fall.isCanceled(),"blocked ground takeoff granted grace");check(b.condition().equals(BroomItemData.FRESH),"blocked takeoff advanced wear");h.succeed();
 }
 @GameTest(template=ARENA) public static void mountedBroomBelongsToChunkSaveAndNeverThePlayerRootVehicle(GameTestHelper h){floor(h);var actor=witch(h,"broom_save_owner",AT.offset(0,0,-2));var b=broom(h,actor);up(actor,b);actor.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,io.github.dirtyredz.herbalwitch.magic.wand.fixture.WandFixtures.wand(io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode.WATER));check(io.github.dirtyredz.herbalwitch.magic.wand.use.WandUseService.use(actor,net.minecraft.world.InteractionHand.MAIN_HAND).equals("riding"),"mounted wand cast allowed");check(b.shouldBeSaved(),"mounted broom excluded from chunk save");var player=actor.saveWithoutId(new CompoundTag());check(!player.contains("RootVehicle"),"player save duplicated physical broom");var tag=new CompoundTag();check(b.save(tag)&&tag.getString("id").equals("herbalwitch:magic/broom/broom"),"chunk cannot serialize broom");actor.stopRiding();h.succeed();}
}
