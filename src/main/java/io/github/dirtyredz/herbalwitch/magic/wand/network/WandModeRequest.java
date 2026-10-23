package io.github.dirtyredz.herbalwitch.magic.wand.network;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode;
import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
/** Exact observed held stack and slot, never an action or a price. */
public record WandModeRequest(InteractionHand hand,int slot,ItemStack observed,WandMode mode){
 public WandModeRequest{observed=observed.copy();}
 public void encode(FriendlyByteBuf b){b.writeEnum(hand);b.writeVarInt(slot);b.writeItem(observed);b.writeEnum(mode);}
 public static WandModeRequest decode(FriendlyByteBuf b){PayloadBounds.requireAtMost(b,PayloadBounds.MAX_REQUEST_BYTES);var hand=PayloadBounds.readEnum(b,InteractionHand.class);int slot=b.readVarInt();if(slot<0||slot>40)throw new io.netty.handler.codec.DecoderException("Invalid wand slot");return new WandModeRequest(hand,slot,b.readItem(),PayloadBounds.readEnum(b,WandMode.class));}
}
