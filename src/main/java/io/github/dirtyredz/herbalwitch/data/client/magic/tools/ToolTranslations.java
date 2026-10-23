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
 }
}
