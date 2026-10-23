package io.github.dirtyredz.herbalwitch.magic.wand;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
import io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode;
import io.github.dirtyredz.herbalwitch.magic.wand.network.WandModeRequest;
import io.github.dirtyredz.herbalwitch.magic.broom.network.BroomInput;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;
@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class WandNetworkGameTests {
 @GameTest(template=TEMPLATE) public static void selectorRoundTripsSevenModesAndBothToolRequestsRejectOversizedPayloads(GameTestHelper h){for(var mode:WandMode.values()){var b=new FriendlyByteBuf(Unpooled.buffer());try{var request=new WandModeRequest(net.minecraft.world.InteractionHand.OFF_HAND,40,io.github.dirtyredz.herbalwitch.magic.wand.fixture.WandFixtures.wand(mode),mode);request.encode(b);var read=WandModeRequest.decode(b);check(read.mode()==mode&&read.slot()==40&&read.hand()==request.hand(),"selector wire mismatch");}finally{b.release();}}for(int type=0;type<2;type++){var b=new FriendlyByteBuf(Unpooled.buffer());try{b.writeZero(type==0?4097:28);boolean rejected=false;try{if(type==0)WandModeRequest.decode(b);else BroomInput.decode(b);}catch(io.netty.handler.codec.DecoderException expected){rejected=true;}check(rejected,"oversized tool request accepted");}finally{b.release();}}h.succeed();}
}
