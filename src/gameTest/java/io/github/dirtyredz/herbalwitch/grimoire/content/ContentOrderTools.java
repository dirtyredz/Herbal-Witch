package io.github.dirtyredz.herbalwitch.grimoire.content;
import java.util.List;
import java.util.ArrayList;
/** Independent M10 additions preserve the complete earlier ordering. */
final class ContentOrderTools {
 static final List<String> STATIONS=List.of("station|herbalwitch:stations/tools/broom_stand/station","station|herbalwitch:stations/tools/wand_bench/station");
 static final List<String> RECIPES=List.of("recipe|herbalwitch:processing/broom_work/craft","recipe|herbalwitch:processing/broom_work/repair","recipe|herbalwitch:processing/wand_carving/craft");
 static final List<String> ADDITIONS=java.util.stream.Stream.concat(STATIONS.stream(),RECIPES.stream()).toList();
 private ContentOrderTools(){}
 static List<String> expected(){var out=new ArrayList<>(ContentOrderFamiliars.expected());out.addAll(out.indexOf("station|herbalwitch:stations/charm_workbench/station")+1,STATIONS);int last=0;for(int i=0;i<out.size();i++)if(out.get(i).startsWith("recipe|"))last=i;out.addAll(last+1,RECIPES);return List.copyOf(out);}
}
