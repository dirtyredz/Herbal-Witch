package io.github.dirtyredz.herbalwitch.magic.broom.flight;
import io.github.dirtyredz.herbalwitch.magic.broom.persistence.BroomItemData;
/** Ground contact ends a landing; fresh input cannot cancel an airborne forced descent. */
public enum BroomFlightState {
 GROUNDED, FLYING, LANDING;
 public BroomFlightState next(boolean permitted,boolean ascend,boolean grounded) {
  if(this==LANDING) return grounded?GROUNDED:LANDING;
  if(this==FLYING) return permitted?FLYING:LANDING;
  return permitted && ascend?FLYING:GROUNDED;
 }
 public static BroomItemData worn(BroomItemData before,int interval,int remainder) {
  if(interval<20 || interval>24000)throw new IllegalArgumentException("Wear interval outside 20..24000");
  int total=before.wearTicks()+1;
  return new BroomItemData(Math.min(600,before.wear()+total/interval),total%interval,remainder);
 }
}
