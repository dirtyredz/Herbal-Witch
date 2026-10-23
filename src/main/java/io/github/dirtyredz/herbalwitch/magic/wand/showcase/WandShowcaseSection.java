package io.github.dirtyredz.herbalwitch.magic.wand.showcase;
import io.github.dirtyredz.herbalwitch.showcase.layout.*;
import io.github.dirtyredz.herbalwitch.apothecary.stations.showcase.StationShowcaseSection;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.magic.wand.registry.WandItems;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.crop.HerbCropBlock;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import java.util.*;
public final class WandShowcaseSection implements ShowcaseSection {
 private final StationShowcaseSection station=new StationShowcaseSection(StationCatalog.WAND_BENCH,false,"showcase.herbalwitch.recipe.tools/wand_bench",()->Map.of(0,new ItemStack(Items.STICK,2),1,new ItemStack(Items.AMETHYST_SHARD),2,new ItemStack(Items.GOLD_NUGGET)));
 public String id(){return station.id();}public String titleKey(){return station.titleKey();}
 public List<ShowcasePlacement> placements(){var out=new ArrayList<>(station.placements());out.add(ShowcasePlacement.label(0,4,ShowcasePlacement.Line.translated("showcase.herbalwitch.wand.supplies")));out.add(ShowcasePlacement.content(1,1,4,()->Blocks.BARREL.defaultBlockState(),be->{if(be instanceof BarrelBlockEntity b){b.setItem(0,new ItemStack(WandItems.WAND.get()));b.setItem(1,new ItemStack(Items.WHEAT_SEEDS,16));b.setItem(2,new ItemStack(Items.CARROT,16));b.setItem(3,new ItemStack(Items.STICK,16));}}));
 out.add(ShowcasePlacement.label(0,7,ShowcasePlacement.Line.translated("showcase.herbalwitch.wand.harvest")));out.add(ShowcasePlacement.floor(1,7,()->Blocks.FARMLAND.defaultBlockState()));out.add(ShowcasePlacement.content(1,1,7,()->((CropBlock)Blocks.WHEAT).getStateForAge(7)));
 out.add(ShowcasePlacement.label(0,10,ShowcasePlacement.Line.translated("showcase.herbalwitch.wand.herbs")));out.add(ShowcasePlacement.content(1,1,10,()->BotanyBlocks.HERB_BED.get().defaultBlockState()));out.add(ShowcasePlacement.content(1,2,10,()->{var crop=(HerbCropBlock)BotanyBlocks.plant(HerbCatalog.SAGE);return crop.getStateForAge(crop.getMaxAge());}));
 out.add(ShowcasePlacement.label(0,13,ShowcasePlacement.Line.translated("showcase.herbalwitch.wand.water")));out.add(ShowcasePlacement.content(1,1,13,()->BotanyBlocks.HERB_BED.get().defaultBlockState()));
 out.add(ShowcasePlacement.label(0,16,ShowcasePlacement.Line.translated("showcase.herbalwitch.wand.light")));out.add(ShowcasePlacement.content(1,1,16,()->Blocks.CANDLE.defaultBlockState()));return out;}
}
