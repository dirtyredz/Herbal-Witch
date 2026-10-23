package io.github.dirtyredz.herbalwitch.magic.broom.showcase;
import io.github.dirtyredz.herbalwitch.showcase.layout.*;
import io.github.dirtyredz.herbalwitch.apothecary.stations.showcase.StationShowcaseSection;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.magic.broom.registry.BroomItems;
import io.github.dirtyredz.herbalwitch.magic.broom.persistence.BroomItemData;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import java.util.*;
/** The stand and its practice supplies share a narrow plot in the existing witchcraft rows. */
public final class BroomShowcaseSection implements ShowcaseSection {
 private final StationShowcaseSection station=new StationShowcaseSection(StationCatalog.BROOM_STAND,false,"showcase.herbalwitch.recipe.tools/broom_stand",()->Map.of(0,new ItemStack(Items.STICK,2),1,new ItemStack(Items.WHEAT,3),2,new ItemStack(Items.STRING,2)));
 public String id(){return station.id();}public String titleKey(){return station.titleKey();}
 public List<ShowcasePlacement> placements(){var out=new ArrayList<>(station.placements());out.add(ShowcasePlacement.label(0,4,ShowcasePlacement.Line.translated("showcase.herbalwitch.broom.supplies")));out.add(ShowcasePlacement.content(1,1,4,()->Blocks.BARREL.defaultBlockState(),be->{if(be instanceof BarrelBlockEntity b){b.setItem(0,new ItemStack(BroomItems.BROOM.get()));var worn=new ItemStack(BroomItems.BROOM.get());new BroomItemData(400,100,7).write(worn);b.setItem(1,worn);b.setItem(2,new ItemStack(Items.WHEAT,16));b.setItem(3,new ItemStack(Items.STRING,16));}}));out.add(ShowcasePlacement.label(0,7,ShowcasePlacement.Line.translated("showcase.herbalwitch.broom.pad")));out.addAll(ShowcasePlacement.floorArea(1,7,2,7,()->Blocks.POLISHED_ANDESITE.defaultBlockState()));return out;}
}
