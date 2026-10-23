package io.github.dirtyredz.herbalwitch.magic.broom.flight;
import io.github.dirtyredz.herbalwitch.magic.broom.persistence.BroomItemData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BroomStateTest {
 @Test void wearUsesTheConfiguredIntervalIncludingAReducedInterval() {
  assertEquals(new BroomItemData(1,0,3),BroomFlightState.worn(new BroomItemData(0,199,3),200,3));
  assertEquals(new BroomItemData(120,0,3),BroomFlightState.worn(new BroomItemData(0,23999,3),200,3));
  assertEquals(new BroomItemData(600,0,3),BroomFlightState.worn(new BroomItemData(599,23999,3),20,3));
 }
 @Test void onlyGroundedBroomsCanTakeOffAndLandingCannotBeCancelledInAir() {
  assertEquals(BroomFlightState.FLYING,BroomFlightState.GROUNDED.next(true,true,false));
  assertEquals(BroomFlightState.LANDING,BroomFlightState.FLYING.next(false,true,false));
  assertEquals(BroomFlightState.LANDING,BroomFlightState.LANDING.next(true,true,false));
  assertEquals(BroomFlightState.GROUNDED,BroomFlightState.LANDING.next(true,true,true));
 }
}
