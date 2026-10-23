package io.github.dirtyredz.herbalwitch.magic.broom.network;
import io.github.dirtyredz.herbalwitch.magic.broom.config.BroomServerConfig;
import io.github.dirtyredz.herbalwitch.magic.wand.config.WandServerConfig;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchRulesUpdate;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class BroomRulesTest {
 @Test void toolsAreSyncedAndEveryLimitIsValidated(){
  var d=WitchRules.DEFAULTS;var broom=new BroomServerConfig(false,List.of("minecraft:overworld"),16,8,20,180,.5,2000,24000,400);var wand=new WandServerConfig(false,6,100,List.of(0,2000,100,101,1),1,1,.5);
  var rules=new WitchRules(d.foundingEnabled(),d.initiationEnabled(),d.foundingTicks(),d.initiationTicks(),d.attunementTicks(),d.inviteExpiry(),d.inviteCooldown(),d.energy(),d.workings(),broom,wand);assertTrue(rules.problems().isEmpty());assertNotEquals(d.fingerprint(),rules.fingerprint());var buffer=new FriendlyByteBuf(Unpooled.buffer());try{new WitchRulesUpdate(rules).encode(buffer);assertEquals(rules,WitchRulesUpdate.decode(buffer).rules());assertFalse(buffer.isReadable());}finally{buffer.release();}
  assertFalse(new BroomServerConfig(true,List.of(),Double.NaN,4,8,120,2,0,200,200).problems().isEmpty());assertFalse(new BroomServerConfig(true,List.of(),8,4,8,120,2,0,19,200).problems().isEmpty());assertFalse(new WandServerConfig(true,7,10,List.of(100,100,100,100,100),16,8,2).problems().isEmpty());
 }
 @Test void malformedInputNeverDecodes(){for(int flags:new int[]{128,255}){var b=new FriendlyByteBuf(Unpooled.buffer());try{new BroomInput(UUID.randomUUID(),0,flags).encode(b);assertThrows(io.netty.handler.codec.DecoderException.class,()->BroomInput.decode(b));}finally{b.release();}}}
}
