package io.github.dirtyredz.herbalwitch.magic.broom.network;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import io.github.dirtyredz.herbalwitch.platform.network.Messages;
import io.github.dirtyredz.herbalwitch.magic.broom.entity.BroomEntity;
public final class BroomPackets {
 public record Lease(int entity,UUID nonce){public void encode(FriendlyByteBuf b){b.writeVarInt(entity);b.writeUUID(nonce);}public static Lease decode(FriendlyByteBuf b){return new Lease(b.readVarInt(),b.readUUID());}}
 private static Consumer<Lease> client=m->{};
 private BroomPackets(){}
 public static void client(Consumer<Lease> listener){client=listener;}
 public static void register(){Messages.toServer(BroomInput.class,BroomInput::encode,BroomInput::decode,BroomPackets::handle);Messages.toClient(Lease.class,Lease::encode,Lease::decode,m->client.accept(m));}
 public static void handle(ServerPlayer actor,BroomInput m){if(!(actor instanceof net.minecraftforge.common.util.FakePlayer)&&actor.getVehicle() instanceof BroomEntity broom)broom.input(actor,m);}
 public static void lease(ServerPlayer actor,BroomEntity broom,UUID nonce){Messages.send(actor,new Lease(broom.getId(),nonce));}
}
