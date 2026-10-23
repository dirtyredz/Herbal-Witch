package io.github.dirtyredz.herbalwitch.magic.broom.config;
import net.minecraftforge.common.ForgeConfigSpec;
import java.util.List;
/** Feature-owned bindings in the single witch server config. */
public final class BroomConfigValues {
 private final ForgeConfigSpec.BooleanValue enabled;
 private final ForgeConfigSpec.ConfigValue<List<? extends String>> dimensions;
 private final ForgeConfigSpec.DoubleValue speed,vertical,acceleration,turn,landing;
 private final ForgeConfigSpec.IntValue energy,wear,protection;
 public BroomConfigValues(ForgeConfigSpec.Builder b){b.push("broom");
  enabled=b.define("enabled",true);dimensions=b.comment("Empty permits every dimension; at most 32 resource IDs.").defineListAllowEmpty("dimensions",List.of(),o->o instanceof String s&&s.length()<=128&&net.minecraft.resources.ResourceLocation.tryParse(s)!=null);
  speed=b.defineInRange("speed",8D,1,16);vertical=b.defineInRange("verticalSpeed",4D,1,8);acceleration=b.defineInRange("acceleration",8D,1,20);turn=b.defineInRange("turnRate",120D,30,180);landing=b.defineInRange("landingSpeed",2D,.5,4);
  energy=b.comment("Hundredths of energy per airborne second.").defineInRange("energyPerSecond",0,0,2000);wear=b.defineInRange("wearInterval",200,20,24000);protection=b.defineInRange("fallProtection",200,0,400);b.pop();
 }
 public BroomServerConfig read(){return new BroomServerConfig(enabled.get(),List.copyOf(dimensions.get()),speed.get(),vertical.get(),acceleration.get(),turn.get(),landing.get(),energy.get(),wear.get(),protection.get());}
}
