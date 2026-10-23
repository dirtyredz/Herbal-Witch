package io.github.dirtyredz.herbalwitch.magic.wand.grimoire;
import io.github.dirtyredz.herbalwitch.grimoire.content.*;
import io.github.dirtyredz.herbalwitch.magic.wand.registry.WandItems;
import io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.util.function.Consumer;
public final class WandEntries implements GrimoireContentSource {
 public static final int PRIORITY=1210;
 public static final EntryKey KEY=EntryKey.of(EntryKey.Kind.GUIDE,"herbalwitch:wand/utilities");
 public void contribute(GrimoireContent.Inputs inputs,Consumer<GrimoireEntry> out){var r=WitchRules.current().wand();var lines=new ArrayList<Component>();lines.add(Component.translatable("guide.herbalwitch.wand.obtain"));lines.add(Component.translatable("guide.herbalwitch.wand.rules",r.reach(),r.cooldown(),r.scanned(),r.stacks(),r.radius()));for(var mode:WandMode.values()){lines.add(Component.translatable("guide.herbalwitch.wand.row",Component.translatable("wand.herbalwitch.mode."+mode.id()),Component.translatable("wand.herbalwitch.action."+mode.id())));if(mode.ordinal()<5)lines.add(Component.translatable("guide.herbalwitch.wand.cost",r.costs().get(mode.ordinal())/100D));}lines.add(Component.translatable("guide.herbalwitch.wand.delegation"));out.accept(GrimoireEntry.page(KEY,GrimoireContent.WITCHCRAFT,Component.translatable("guide.herbalwitch.wand.title"),WandItems.WAND.get().getDefaultInstance(),lines,List.of(EntryKey.of(EntryKey.Kind.STATION,"herbalwitch:stations/tools/wand_bench/station"),EntryKey.of(EntryKey.Kind.RECIPE,"herbalwitch:processing/wand_carving/craft"),io.github.dirtyredz.herbalwitch.grimoire.catalog.WitchcraftKeys.key("familiar"),io.github.dirtyredz.herbalwitch.grimoire.catalog.WitchcraftKeys.key("brewing"),io.github.dirtyredz.herbalwitch.grimoire.catalog.WitchcraftKeys.key("wards"),EntryKey.of(EntryKey.Kind.STATION,"herbalwitch:magic/ritual/altar"),EntryKey.of(EntryKey.Kind.GUIDE,"herbalwitch:familiars/commands"),EntryKey.of(EntryKey.Kind.GUIDE,"herbalwitch:familiars/recovery"))));}
}
