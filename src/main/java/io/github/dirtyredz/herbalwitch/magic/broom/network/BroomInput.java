package io.github.dirtyredz.herbalwitch.magic.broom.network;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.handler.codec.DecoderException;
/** Intent only; never accepts position, velocity, price or an entity chosen by the client. */
public record BroomInput(UUID nonce,long sequence,int flags){
 public void encode(FriendlyByteBuf b){b.writeUUID(nonce);b.writeVarLong(sequence);b.writeByte(flags);}
 public static BroomInput decode(FriendlyByteBuf b){io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds.requireAtMost(b,27);var m=new BroomInput(b.readUUID(),b.readVarLong(),b.readUnsignedByte());if(m.sequence<0||(m.flags&~127)!=0)throw new DecoderException("Invalid broom intent");return m;}
}
