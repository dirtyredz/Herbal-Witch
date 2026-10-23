package io.github.dirtyredz.herbalwitch.magic.witch.network;
import io.github.dirtyredz.herbalwitch.magic.broom.config.BroomServerConfig;
import io.github.dirtyredz.herbalwitch.magic.wand.config.WandServerConfig;
import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import java.util.ArrayList;
import net.minecraft.network.FriendlyByteBuf;
/** Fixed-size numeric settings plus a bounded dimension allowlist. */
final class ToolRulesCodec {
 private ToolRulesCodec(){}
 static void broom(FriendlyByteBuf b,BroomServerConfig v){b.writeBoolean(v.enabled());b.writeVarInt(v.dimensions().size());for(String s:v.dimensions())b.writeUtf(s,128);for(double n:new double[]{v.speed(),v.verticalSpeed(),v.acceleration(),v.turnRate(),v.landingSpeed()})b.writeDouble(n);b.writeVarInt(v.energyPerSecond());b.writeVarInt(v.wearInterval());b.writeVarInt(v.fallProtection());}
 static BroomServerConfig broom(FriendlyByteBuf b){boolean enabled=b.readBoolean();int n=PayloadBounds.readCount(b,32);var dims=new ArrayList<String>();for(int i=0;i<n;i++)dims.add(b.readUtf(128));return new BroomServerConfig(enabled,dims,b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readVarInt(),b.readVarInt(),b.readVarInt());}
 static void wand(FriendlyByteBuf b,WandServerConfig v){b.writeBoolean(v.enabled());b.writeDouble(v.reach());b.writeVarInt(v.cooldown());for(int cost:v.costs())b.writeVarInt(cost);b.writeVarInt(v.scanned());b.writeVarInt(v.stacks());b.writeDouble(v.radius());}
 static WandServerConfig wand(FriendlyByteBuf b){boolean enabled=b.readBoolean();double reach=b.readDouble();int cooldown=b.readVarInt();var costs=new ArrayList<Integer>();for(int i=0;i<5;i++)costs.add(b.readVarInt());return new WandServerConfig(enabled,reach,cooldown,costs,b.readVarInt(),b.readVarInt(),b.readDouble());}
}
