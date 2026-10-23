package io.github.dirtyredz.herbalwitch.magic.broom.grimoire;
import io.github.dirtyredz.herbalwitch.grimoire.content.*;
import io.github.dirtyredz.herbalwitch.magic.broom.registry.BroomItems;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.util.function.Consumer;
/** Feature-owned guide quotes synchronized server rules and links live production recipes. */
public final class BroomEntries implements GrimoireContentSource {
 public static final int PRIORITY=1200;
 public static final EntryKey KEY=EntryKey.of(EntryKey.Kind.GUIDE,"herbalwitch:broom/flight");
 public void contribute(GrimoireContent.Inputs inputs,Consumer<GrimoireEntry> out){var r=WitchRules.current().broom();out.accept(GrimoireEntry.page(KEY,GrimoireContent.WITCHCRAFT,Component.translatable("guide.herbalwitch.broom.title"),BroomItems.BROOM.get().getDefaultInstance(),List.of(
  Component.translatable("guide.herbalwitch.broom.obtain"),Component.translatable("guide.herbalwitch.broom.controls"),Component.translatable("guide.herbalwitch.broom.rules",r.speed(),r.verticalSpeed(),r.energyPerSecond()/100D,r.wearInterval(),io.github.dirtyredz.herbalwitch.magic.broom.catalog.BroomSpec.MAX_WEAR),Component.translatable("guide.herbalwitch.broom.lifecycle",r.fallProtection()/20D),Component.translatable("guide.herbalwitch.broom.enabled",r.enabled(),r.dimensions().isEmpty()?"all":String.join(", ",r.dimensions()))),List.of(EntryKey.of(EntryKey.Kind.STATION,"herbalwitch:stations/tools/broom_stand/station"),EntryKey.of(EntryKey.Kind.RECIPE,"herbalwitch:processing/broom_work/craft"),EntryKey.of(EntryKey.Kind.RECIPE,"herbalwitch:processing/broom_work/repair"))));}
}
