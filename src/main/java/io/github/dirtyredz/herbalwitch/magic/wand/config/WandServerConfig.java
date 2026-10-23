package io.github.dirtyredz.herbalwitch.magic.wand.config;
import java.util.List;
/** Five local utility prices are hundredths; focus and familiars retain their own service prices. */
public record WandServerConfig(boolean enabled,double reach,int cooldown,List<Integer> costs,int scanned,int stacks,double radius){
 public static final WandServerConfig DEFAULTS=new WandServerConfig(true,5,10,List.of(100,100,100,100,100),16,8,2);
 public WandServerConfig{costs=List.copyOf(costs);}
 public List<String> problems(){return !Double.isFinite(reach)||reach<1||reach>6||cooldown<5||cooldown>100||costs.size()!=5||costs.stream().anyMatch(c->c<0||c>2000)||scanned<1||scanned>16||stacks<1||stacks>8||!Double.isFinite(radius)||radius<=0||radius>2?List.of("Invalid wand settings"):List.of();}
}
