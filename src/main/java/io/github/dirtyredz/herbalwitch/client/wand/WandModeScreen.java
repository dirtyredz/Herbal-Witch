package io.github.dirtyredz.herbalwitch.client.wand;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode;
import io.github.dirtyredz.herbalwitch.magic.wand.network.WandModeRequest;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.platform.network.Messages;
/** Selecting a preference sends no cast; the observed hand/slot/stack must still match on the server. */
public final class WandModeScreen extends Screen {
 private final InteractionHand hand;private final int slot;private final ItemStack observed;
 public WandModeScreen(InteractionHand hand,int slot,ItemStack observed){super(Component.translatable("screen.herbalwitch.wand.title"));this.hand=hand;this.slot=slot;this.observed=observed.copy();}
 @Override protected void init(){int y=height/2-75;for(var mode:WandMode.values()){
  var label=Component.translatable("wand.herbalwitch.mode."+mode.id());
  if(mode.ordinal()<5)label.append(Component.translatable("screen.herbalwitch.wand.cost",String.format(java.util.Locale.ROOT,"%.2f",WitchRules.current().wand().costs().get(mode.ordinal())/100D)));
  addRenderableWidget(Button.builder(label,b->{Messages.sendToServer(new WandModeRequest(hand,slot,observed,mode));onClose();}).bounds(width/2-105,y,210,20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("wand.herbalwitch.action."+mode.id()))).build());y+=22;
 }}
 @Override public void render(GuiGraphics g,int x,int y,float partial){renderBackground(g);g.drawCenteredString(font,title,width/2,height/2-100,0xF2E8CF);super.render(g,x,y,partial);}
 @Override public boolean isPauseScreen(){return false;}
}
