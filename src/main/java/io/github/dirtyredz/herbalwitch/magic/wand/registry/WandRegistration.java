package io.github.dirtyredz.herbalwitch.magic.wand.registry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;
import io.github.dirtyredz.herbalwitch.magic.wand.use.WandUseService;
import io.github.dirtyredz.herbalwitch.magic.wand.network.WandPackets;
import io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode;
import io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalInteractionEvents;
public final class WandRegistration {
 public static void showcase(){io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry.register(new io.github.dirtyredz.herbalwitch.magic.wand.showcase.WandShowcaseSection());}
 private WandRegistration(){}
 public static void register(IEventBus modBus){io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent.register(io.github.dirtyredz.herbalwitch.magic.wand.grimoire.WandEntries.PRIORITY,new io.github.dirtyredz.herbalwitch.magic.wand.grimoire.WandEntries());
  WandPackets.register();var bus=MinecraftForge.EVENT_BUS;bus.addListener(WandUseService::logout);bus.addListener(WandUseService::stopped);bus.addListener(WandPackets::logout);bus.addListener(WandPackets::stopped);
  io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarMenuAccess.focus(WandUseService::focus);
  AnimalInteractionEvents.tool(new AnimalInteractionEvents.ToolRoute(){public boolean matches(net.minecraft.world.item.ItemStack stack){return WandUseService.mode(stack)==WandMode.FAMILIAR;}public void interact(net.minecraft.server.level.ServerPlayer actor,net.minecraft.world.entity.Mob mob,net.minecraft.world.InteractionHand hand){WandUseService.message(actor,WandUseService.use(actor,hand,mob));}});
 }
}
