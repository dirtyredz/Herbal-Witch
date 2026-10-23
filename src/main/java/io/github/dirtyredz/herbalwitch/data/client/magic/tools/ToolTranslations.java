package io.github.dirtyredz.herbalwitch.data.client.magic.tools;
import net.minecraftforge.common.data.LanguageProvider;
/** Tool-owned language contribution to the sole locale writer. */
public final class ToolTranslations {
 private ToolTranslations(){}
 public static void addTo(LanguageProvider lang){
  lang.add("item.herbalwitch.magic.broom.broom","Flying Broom");
  lang.add("item.herbalwitch.magic.wand.wand","Witch's Wand");
  lang.add("recipe.herbalwitch.broom.repair","Up to +%s condition");
  lang.add("screen.herbalwitch.broom.condition","Condition: %s -> %s");
  lang.add("entity.herbalwitch.magic.broom.broom","Flying Broom");
  lang.add("key.categories.herbalwitch","The Herbal Witch");
  lang.add("key.herbalwitch.broom.descend","Broom: descend");
  lang.add("key.herbalwitch.broom.land","Broom: land");
  lang.add("hud.herbalwitch.broom.condition","Broom %s / 600 - %s");
  lang.add("hud.herbalwitch.broom.controls","%s rise | %s descend | %s land | %s dismount");
  lang.add("broom.herbalwitch.state.grounded","Grounded");
  lang.add("broom.herbalwitch.state.flying","Flying");
  lang.add("broom.herbalwitch.state.landing","Landing");
  lang.add("tooltip.herbalwitch.broom.condition","Condition: %s / 600");
  lang.add("tooltip.herbalwitch.broom.use","Place to deploy. Owner use to ride; sneak-use when grounded to pack.");
  lang.add("tooltip.herbalwitch.broom.invalid","Unrecognized broom data: preserved, but unusable.");
  lang.add("broom.herbalwitch.refused","Cannot use this broom: check ownership, witch status, condition and free inventory space.");
  lang.add("key.herbalwitch.wand.select","Wand: choose utility");
  lang.add("screen.herbalwitch.wand.title","Wand utilities");
  lang.add("screen.herbalwitch.wand.cost"," ? %s energy");
  lang.add("hud.herbalwitch.wand","%s | %s choose utility");
  lang.add("tooltip.herbalwitch.wand.mode","Utility: %s");
  lang.add("tooltip.herbalwitch.wand.invalid","Unrecognized wand data: preserved, but unusable.");
  lang.add("wand.herbalwitch.mode.harvest","Harvest");
  lang.add("wand.herbalwitch.mode.plant","Plant");
  lang.add("wand.herbalwitch.mode.water","Water");
  lang.add("wand.herbalwitch.mode.collect","Collect");
  lang.add("wand.herbalwitch.mode.light","Light");
  lang.add("wand.herbalwitch.mode.focus","Ritual and ward focus");
  lang.add("wand.herbalwitch.mode.familiar","Familiar commands");
  lang.add("wand.herbalwitch.action.harvest","Harvest one mature herb or vanilla crop and leave it growing.");
  lang.add("wand.herbalwitch.action.plant","Plant one seed from your offhand on suitable soil.");
  lang.add("wand.herbalwitch.action.water","Moisten one farmland or herb bed without changing its enrichment.");
  lang.add("wand.herbalwitch.action.collect","Gather nearby loose items at your aim point into available inventory space.");
  lang.add("wand.herbalwitch.action.light","Light an unlit candle, campfire, or fueled hearth or stove.");
  lang.add("wand.herbalwitch.action.focus","Open an altar, ritual circle or ward. Their normal ownership and costs apply.");
  lang.add("wand.herbalwitch.action.familiar","Use on your loaded familiar for commands; use elsewhere for your roster.");
  lang.add("wand.herbalwitch.result.success","Wand utility ready.");
  lang.add("wand.herbalwitch.result.rate","Slow down before using the wand again.");
  lang.add("wand.herbalwitch.result.busy","Finish the current wand action first.");
  lang.add("wand.herbalwitch.result.invalid","This wand has unrecognized data.");
  lang.add("wand.herbalwitch.result.disabled","Wand utilities are disabled on this server.");
  lang.add("wand.herbalwitch.result.witch","Only a living witch can use this wand.");
  lang.add("wand.herbalwitch.result.riding","Land and dismount before using a wand.");
  lang.add("wand.herbalwitch.result.target","No valid target: check the selected utility, reach, access and supplies.");
  lang.add("wand.herbalwitch.result.cooldown","The wand is recovering.");
  lang.add("wand.herbalwitch.result.energy","Not enough energy for this utility.");
  lang.add("wand.herbalwitch.result.changed","The target or supplies changed; nothing was spent.");
 }
}
