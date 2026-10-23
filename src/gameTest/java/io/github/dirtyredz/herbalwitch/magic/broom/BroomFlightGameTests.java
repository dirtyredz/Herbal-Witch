package io.github.dirtyredz.herbalwitch.magic.broom;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.broom.flight.*;
import io.github.dirtyredz.herbalwitch.magic.broom.persistence.BroomItemData;
import io.github.dirtyredz.herbalwitch.magic.broom.network.*;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import static io.github.dirtyredz.herbalwitch.magic.broom.fixture.BroomFixtures.*;
import static io.github.dirtyredz.herbalwitch.magic.broom.fixture.BroomFixtures.witch;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
@GameTestHolder(HerbalWitch.MOD_ID) @PrefixGameTestTemplate(false)
public final class BroomFlightGameTests {
 @GameTest(template=ARENA) public static void authenticatedInputClimbsAndRejectsOtherMounts(GameTestHelper h){
  floor(h);var actor=witch(h,"broom_flight",AT.offset(0,0,-2));var b=broom(h,actor);double y=b.getY();up(actor,b);var nonce=b.controls().nonce();
  check(!b.input(actor,new BroomInput(nonce,0,0)),"replay accepted");check(!b.input(actor,new BroomInput(java.util.UUID.randomUUID(),1,0)),"foreign lease accepted");
  for(int i=0;i<20;i++)b.tick();check(b.getY()>y+1&&b.flight()==BroomFlightState.FLYING,"did not climb");check(b.getControllingPassenger()==null,"vanilla authority assigned");actor.stopRiding();check(b.controls()==null&&b.flight()==BroomFlightState.LANDING,"dismount kept controls");for(int i=0;i<80;i++)b.tick();check(b.onGround()&&b.flight()==BroomFlightState.GROUNDED,"did not land");h.succeed();
 }
 @GameTest(template=ARENA) public static void fractionalEnergyAndWearCommitOnlyOnPaidFlightTicks(GameTestHelper h){
  var old=WitchRules.server();try{floor(h);cost(1);var actor=witch(h,"broom_paid",AT.offset(0,0,-2));var b=broom(h,actor);b.condition(new BroomItemData(0,199,19));int before=WitchStatusService.energyUnits(actor);up(actor,b);b.tick();check(WitchStatusService.energyUnits(actor)==before-1&&b.condition().equals(new BroomItemData(1,0,0)),"fractional debit/wear");check(io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.practice(actor)==0,"passive upkeep awarded practice");actor.stopRiding();b.discard();
  cost(2000);var depleted=witch(h,"broom_depleted",AT.offset(0,0,-2));WitchStatusService.spend(depleted,WitchStatusService.energyUnits(depleted)-50,()->true);var empty=broom(h,depleted);up(depleted,empty);empty.tick();check(WitchStatusService.energyUnits(depleted)==50&&empty.condition().equals(BroomItemData.FRESH)&&empty.flight()!=BroomFlightState.FLYING,"failed debit changed accumulators");depleted.stopRiding();h.succeed();}finally{WitchRules.applyServer(old);}
 }
 @GameTest(template=ARENA) public static void riderClearanceStopsAtCeilingAndUnknownTerrainIsNotLoaded(GameTestHelper h){
  floor(h);for(int x=5;x<12;x++)for(int z=5;z<12;z++)h.setBlock(new net.minecraft.core.BlockPos(x,5,z),net.minecraft.world.level.block.Blocks.STONE);var actor=witch(h,"broom_ceiling",AT.offset(0,0,-2));var b=broom(h,actor);up(actor,b);for(int i=0;i<80;i++)b.tick();check(b.getY()+.2+actor.getBbHeight()<=h.absolutePos(new net.minecraft.core.BlockPos(8,5,8)).getY()+.01,"rider entered ceiling");var far=b.getBoundingBox().move(100000,0,100000);check(!BroomFlightController.loaded(b,far),"unloaded area admitted");actor.stopRiding();h.succeed();
 }
}
