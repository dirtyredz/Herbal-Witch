package io.github.dirtyredz.herbalwitch.magic.wand.config;
import java.util.List;
import java.util.ArrayList;
import net.minecraftforge.common.ForgeConfigSpec;
public final class WandConfigValues {
 private final ForgeConfigSpec.BooleanValue enabled;
 private final ForgeConfigSpec.DoubleValue reach,radius;
 private final ForgeConfigSpec.IntValue cooldown,scanned,stacks;
 private final List<ForgeConfigSpec.IntValue> costs=new ArrayList<>();
 public WandConfigValues(ForgeConfigSpec.Builder b){b.push("wand");enabled=b.define("enabled",true);reach=b.defineInRange("reach",5D,1,6);cooldown=b.defineInRange("cooldown",10,5,100);scanned=b.defineInRange("collectExamined",16,1,16);stacks=b.defineInRange("collectStacks",8,1,8);radius=b.defineInRange("collectRadius",2D,.1,2);for(String key:List.of("harvest","plant","water","collect","light"))costs.add(b.comment("Hundredths of energy per successful cast.").defineInRange(key+"Cost",100,0,2000));b.pop();}
 public WandServerConfig read(){return new WandServerConfig(enabled.get(),reach.get(),cooldown.get(),costs.stream().map(ForgeConfigSpec.IntValue::get).toList(),scanned.get(),stacks.get(),radius.get());}
}
