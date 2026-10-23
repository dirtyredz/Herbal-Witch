package io.github.dirtyredz.herbalwitch.data.magic.tools;
import java.util.Map;
import java.util.Set;
/** Independent exact M10 tool resources. Station resources remain in StationManifest. */
public final class ToolManifest {
 private ToolManifest(){}
 public static Set<String> files(){return Set.of("assets/herbalwitch/models/item/magic/broom/broom.json","assets/herbalwitch/models/item/magic/wand/wand.json");}
 public static Map<String,String> lang(){return Map.ofEntries(
  Map.entry("item.herbalwitch.magic.broom.broom","Flying Broom"),
  Map.entry("item.herbalwitch.magic.wand.wand","Witch's Wand"),
  Map.entry("recipe.herbalwitch.broom.repair","Up to +%s condition"),
  Map.entry("screen.herbalwitch.broom.condition","Condition: %s -> %s"),
  Map.entry("entity.herbalwitch.magic.broom.broom","Flying Broom"),
  Map.entry("key.categories.herbalwitch","The Herbal Witch"),
  Map.entry("key.herbalwitch.broom.descend","Broom: descend"),
  Map.entry("key.herbalwitch.broom.land","Broom: land"),
  Map.entry("hud.herbalwitch.broom.condition","Broom %s / 600 - %s"),
  Map.entry("hud.herbalwitch.broom.controls","%s rise | %s descend | %s land | %s dismount"),
  Map.entry("broom.herbalwitch.state.grounded","Grounded"),
  Map.entry("broom.herbalwitch.state.flying","Flying"),
  Map.entry("broom.herbalwitch.state.landing","Landing"),
  Map.entry("tooltip.herbalwitch.broom.condition","Condition: %s / 600"),
  Map.entry("tooltip.herbalwitch.broom.use","Place to deploy. Owner use to ride; sneak-use when grounded to pack."),
  Map.entry("tooltip.herbalwitch.broom.invalid","Unrecognized broom data: preserved, but unusable."),
  Map.entry("broom.herbalwitch.refused","Cannot use this broom: check ownership, witch status, condition and free inventory space."));}
}
