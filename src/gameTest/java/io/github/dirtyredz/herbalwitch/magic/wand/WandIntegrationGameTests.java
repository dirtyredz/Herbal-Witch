package io.github.dirtyredz.herbalwitch.magic.wand;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;
import io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode;
import io.github.dirtyredz.herbalwitch.magic.wand.use.WandUseService;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures;
import io.github.dirtyredz.herbalwitch.companions.familiars.menu.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.*;
import static io.github.dirtyredz.herbalwitch.magic.wand.fixture.WandFixtures.*;
@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class WandIntegrationGameTests {
 @GameTest(template=TEMPLATE) public static void focusOpensExistingAltarWithoutStartingOrCharging(GameTestHelper h){
  altar(h);var actor=witch(h,"wand_altar",FRONT);int energy=WitchStatusService.energyUnits(actor);cast(h,actor,WandMode.FOCUS,InteractionHand.MAIN_HAND,ALTAR);check(actor.containerMenu instanceof AltarMenu,"focus did not open altar");check(WitchStatusService.energyUnits(actor)==energy,"focus charged before ritual consent");check(io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService.of(actor.getUUID()).isEmpty(),"focus started ritual");h.succeed();
 }
 @GameTest(template=TEMPLATE,timeoutTicks=600,batch="herbalwitch_wand_familiar") public static void familiarWandProvidesExistingRosterAuthorityWithoutAGrimoire(GameTestHelper h){
  FamiliarFixtures.bound(h,"cat","wand_commands",b->h.runAfterDelay(6,()->{
   var actor=b.owner();actor.getInventory().clearContent();actor.setItemInHand(InteractionHand.MAIN_HAND,wand(WandMode.FAMILIAR));check(FamiliarMenuAccess.roster(actor),"wand focus did not authorize roster");io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.setEnergy(actor,0);int energy=WitchStatusService.energyUnits(actor);
   b.mob().moveTo(actor.position().add(0,0,1));var event=new net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract(actor,InteractionHand.MAIN_HAND,b.mob());net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);check(event.isCanceled()&&actor.containerMenu instanceof FamiliarDetailMenu,"owned familiar did not route through existing detail menu");
   check(WandUseService.use(actor,InteractionHand.MAIN_HAND).equals("success")&&actor.containerMenu instanceof FamiliarRosterMenu,"wand roster did not open");
   for(var command:FamiliarCommand.values()){var result=FamiliarCommands.set(actor,b.bond(),command);check(result.equals(command==FamiliarCommand.RETURN_HOME||command==FamiliarCommand.PROTECT_HOME?"no_home":"command_set"),"wand command diverged: "+command+" "+result);}
   check(WitchStatusService.energyUnits(actor)==energy,"ordinary commands charged");actor.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);check(!FamiliarMenuAccess.roster(actor),"removed focus retained access");h.succeed();
  }));
 }
 @GameTest(template=TEMPLATE,batch="herbalwitch_ward_wand_focus") public static void wardFocusKeepsExistingActivationPriceAndOwnerControls(GameTestHelper h){
  floor(h);h.setBlock(TARGET,io.github.dirtyredz.herbalwitch.magic.ward.registry.WardRegistration.STONE.get());var pos=h.absolutePos(TARGET);var owner=witch(h,"wand_ward_owner",FEET);int before=WitchStatusService.energyUnits(owner);cast(h,owner,WandMode.FOCUS,InteractionHand.MAIN_HAND,TARGET);check(owner.containerMenu instanceof io.github.dirtyredz.herbalwitch.magic.ward.menu.WardMenu&&WitchStatusService.energyUnits(owner)==before,"ward focus charged or failed to open");var menu=(io.github.dirtyredz.herbalwitch.magic.ward.menu.WardMenu)owner.containerMenu;
  try{menu.clickMenuButton(owner,io.github.dirtyredz.herbalwitch.magic.ward.menu.WardMenu.Button.ACTIVATE.id());var record=io.github.dirtyredz.herbalwitch.magic.ward.service.WardLifecycle.record(h.getLevel(),pos).orElseThrow();check(record.ownedBy(owner.getUUID())&&WitchStatusService.energyUnits(owner)==before-100*io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules.server().workings().wardActivationCost(),"wand bypassed existing activation price");int radius=record.radius();var rival=witch(h,"wand_ward_rival",FEET);cast(h,rival,WandMode.FOCUS,InteractionHand.MAIN_HAND,TARGET);check(rival.containerMenu instanceof io.github.dirtyredz.herbalwitch.magic.ward.menu.WardMenu,"rival cannot inspect ward");rival.containerMenu.clickMenuButton(rival,io.github.dirtyredz.herbalwitch.magic.ward.menu.WardMenu.Button.RADIUS_DOWN.id());check(io.github.dirtyredz.herbalwitch.magic.ward.service.WardLifecycle.record(h.getLevel(),pos).orElseThrow().radius()==radius,"foreign focus changed ward");}finally{io.github.dirtyredz.herbalwitch.magic.ward.service.WardControls.deactivate(owner,h.getLevel(),pos);io.github.dirtyredz.herbalwitch.magic.ward.service.WardControls.release(owner,h.getLevel(),pos);}h.succeed();
 }
}
