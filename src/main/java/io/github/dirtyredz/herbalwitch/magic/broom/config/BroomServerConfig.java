package io.github.dirtyredz.herbalwitch.magic.broom.config;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.resources.ResourceLocation;
/** Immutable flight settings, installed and synchronized as part of WitchRules. */
public record BroomServerConfig(boolean enabled,List<String> dimensions,double speed,double verticalSpeed,
 double acceleration,double turnRate,double landingSpeed,int energyPerSecond,int wearInterval,int fallProtection) {
 public static final BroomServerConfig DEFAULTS=new BroomServerConfig(true,List.of(),8,4,8,120,2,0,200,200);
 public BroomServerConfig { dimensions=List.copyOf(dimensions); }
 public List<String> problems() {
  var out=new ArrayList<String>();
  if(dimensions.size()>32 || dimensions.stream().anyMatch(s->s.length()>128 || ResourceLocation.tryParse(s)==null))out.add("Invalid broom dimensions");
  if(!range(speed,1,16)||!range(verticalSpeed,1,8)||!range(acceleration,1,20)||!range(turnRate,30,180)||!range(landingSpeed,.5,4))out.add("Invalid broom motion limits");
  if(energyPerSecond<0||energyPerSecond>2000||wearInterval<20||wearInterval>24000||fallProtection<0||fallProtection>400)out.add("Invalid broom cost, wear or protection");
  return out;
 }
 private static boolean range(double v,double low,double high){return Double.isFinite(v)&&v>=low&&v<=high;}
 public boolean permits(ResourceLocation dimension){return enabled&&(dimensions.isEmpty()||dimensions.contains(dimension.toString()));}
}
