package io.github.dirtyredz.herbalwitch.client.stations.tools;
import io.github.dirtyredz.herbalwitch.client.stations.ProcessStationScreen;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.broom_stand.BroomStandMenu;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessStation;
import io.github.dirtyredz.herbalwitch.magic.broom.persistence.BroomItemData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
public final class BroomStandScreen extends ProcessStationScreen<BroomStandMenu> {
 public BroomStandScreen(BroomStandMenu menu,Inventory inventory,Component title) {
  super(menu,inventory,title,new Layout(ResourceLocation.fromNamespaceAndPath("herbalwitch","textures/gui/stations/tools/broom_stand.png"),92,34,-1,0,"broom_work"));
 }
 @Override protected void renderLabels(GuiGraphics graphics,int x,int y) {
  super.renderLabels(graphics,x,y);
  BroomItemData.read(menu.getSlot(4).getItem()).ifPresent(data -> {
   graphics.pose().pushPose(); graphics.pose().scale(0.7F,0.7F,1);
   graphics.drawString(font,
   Component.translatable("screen.herbalwitch.broom.condition",data.condition(),Math.min(600,data.condition()+menu.data(ProcessStation.DATA_COUNT))),100,105,0x404040,false);
   graphics.pose().popPose();
  });
 }
}
