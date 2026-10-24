package io.github.dirtyredz.herbalwitch.companions.animals.showcase;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.AdvancedProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalProfiles;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.showcase.layout.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import java.util.ArrayList;
import java.util.List;

/** Sixteen separate glazed habitats. Supplies demonstrate ordinary interactions without granting care state. */
public final class AnimalShowcaseSection implements ShowcaseSection {
    @Override public String id() { return "animals"; }
    @Override public String titleKey() { return "animal.herbalwitch.title"; }
    @Override public List<ShowcaseEntitySample> entities() {
        List<ShowcaseEntitySample> out = new ArrayList<>();
        for (int i = 0; i < AnimalProfiles.ALL.size(); i++) {
            var p = AnimalProfiles.ALL.get(i);
            out.add(new ShowcaseEntitySample(p.id(), (i % 4) * 8 + 3.5, p.id().endsWith("bat") || p.id().endsWith("parrot") ? 2 : 1,
                    (i / 4) * 7 + 3.5));
        }
        return List.copyOf(out);
    }
    @Override public List<ShowcasePlacement> placements() {
        List<ShowcasePlacement> out = new ArrayList<>();
        for (int i = 0; i < AnimalProfiles.ALL.size(); i++) {
            var profile = AnimalProfiles.ALL.get(i); int x = (i % 4) * 8 + 1, z = (i / 4) * 7; // three clear columns between habitats leave room for the east-side label
            out.add(ShowcasePlacement.sampleLabel(x + 4, z + 1, ShowcasePlacement.Line.translated("entity." + profile.id().replace(':', '.')),
                    ShowcasePlacement.Line.translated("item.herbalwitch.animals.bait." + profile.bait().id())));
            boolean water = profile.id().equals("minecraft:axolotl");
            for (int dx = 0; dx < 5; dx++) for (int dz = 1; dz <= 5; dz++) {
                out.add(ShowcasePlacement.floor(x + dx, z + dz, () -> water ? Blocks.CLAY.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState()));
                out.add(ShowcasePlacement.support(x + dx, 5, z + dz, () -> Blocks.GLASS.defaultBlockState()));
                boolean wall = dx == 0 || dx == 4 || dz == 1 || dz == 5;
                for (int y = 1; y <= 4; y++) {
                    if (wall) {
                        boolean door = !water && dx == 2 && dz == 1 && y <= 2;
                        int height = y;
                        out.add(ShowcasePlacement.support(x + dx, y, z + dz, () -> door
                                ? Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.HALF, height == 1 ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER)
                                : Blocks.GLASS.defaultBlockState()));
                    } else if (water && y <= 2) out.add(ShowcasePlacement.content(x + dx, y, z + dz, () -> Blocks.WATER.defaultBlockState()));
                }
            }
        }
        out.add(ShowcasePlacement.content(32, 1, 0, () -> Blocks.BARREL.defaultBlockState(), entity -> {
            if (entity instanceof BarrelBlockEntity barrel) {
                int slot = 0;
                for (var item : AnimalItems.all()) barrel.setItem(slot++, new ItemStack(item, item == AnimalItems.CHARM.get() ? 1 : 16));
                barrel.setItem(slot++, new ItemStack(PreparationItems.product(EverydayProductCatalog.HEALING_SALVE), 16));
                barrel.setItem(slot++, new ItemStack(AdvancedPreparations.item(AdvancedProductCatalog.SLEEP), 16));
                barrel.setItem(slot, new ItemStack(GrimoireItems.BOOK.get()));
            }
        }));
        out.add(ShowcasePlacement.sampleLabel(32, 0, ShowcasePlacement.Line.translated("animal.herbalwitch.showcase.supplies")));
        return out;
    }
}
