package io.github.dirtyredz.herbalwitch.magic.tools;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import io.github.dirtyredz.herbalwitch.grimoire.content.*;
import io.github.dirtyredz.herbalwitch.magic.wand.grimoire.WandEntries;
import io.github.dirtyredz.herbalwitch.magic.broom.grimoire.BroomEntries;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.wand.config.WandServerConfig;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;
@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class ToolGuidanceGameTests {
 @GameTest(template=TEMPLATE,batch="herbalwitch_tool_guidance") public static void toolGuidePricesFollowLiveRulesAndKeepExistingServiceLinks(GameTestHelper h){var old=WitchRules.server();try{
  var rules=new WitchRules(old.foundingEnabled(),old.initiationEnabled(),old.foundingTicks(),old.initiationTicks(),old.attunementTicks(),old.inviteExpiry(),old.inviteCooldown(),old.energy(),old.workings(),old.broom(),new WandServerConfig(true,3,25,List.of(125,250,375,500,625),12,4,1));check(WitchRules.applyServer(rules).isEmpty(),"guide rules");var entries=new ArrayList<GrimoireEntry>();new BroomEntries().contribute(null,entries::add);new WandEntries().contribute(null,entries::add);var wand=entries.get(1);check(wand.always()&&entries.get(0).always(),"tool guides hidden");var prices=new ArrayList<Double>();for(var line:wand.body())if(line.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t&&t.getKey().equals("guide.herbalwitch.wand.cost"))prices.add(((Number)t.getArgs()[0]).doubleValue());check(prices.equals(List.of(1.25,2.5,3.75,5D,6.25)),"guide cached default prices");check(wand.links().contains(EntryKey.of(EntryKey.Kind.GUIDE,"herbalwitch:witchcraft/wards"))&&wand.links().contains(EntryKey.of(EntryKey.Kind.GUIDE,"herbalwitch:witchcraft/familiar")),"delegated service links absent");h.succeed();}finally{WitchRules.applyServer(old);}}
}
