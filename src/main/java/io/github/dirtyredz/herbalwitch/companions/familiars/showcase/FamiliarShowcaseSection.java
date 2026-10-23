package io.github.dirtyredz.herbalwitch.companions.familiars.showcase;

import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarItems;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.*;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.*;
import io.github.dirtyredz.herbalwitch.magic.charm.registry.CharmItems;
import io.github.dirtyredz.herbalwitch.showcase.layout.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import java.util.*;

/** Nine inert appearance samples and three adjacent-sign supply barrels in the witchcraft group. */
public final class FamiliarShowcaseSection implements ShowcaseSection {
    @Override public String id() { return "familiars"; }
    @Override public String titleKey() { return "showcase.herbalwitch.section.familiars"; }
    @Override public List<ShowcaseEntitySample> entities() {
        var out = new ArrayList<ShowcaseEntitySample>();
        for (int i = 0; i < 9; i++) {
            String species = i == 8 ? "raven" : FamiliarCatalog.ALL.get(i).key();
            out.add(new ShowcaseEntitySample("herbalwitch:familiars/entity/" + species, (i % 3) * 4 + 2.5, 1, (i / 3) * 6 + 2.5, true, i == 8 ? 1 : 0));
        }
        return List.copyOf(out);
    }
    @Override public List<ShowcasePlacement> placements() {
        var out = new ArrayList<ShowcasePlacement>();
        for (int i = 0; i < 9; i++) {
            int x = (i % 3) * 4, z = (i / 3) * 6; String species = i == 8 ? "crow" : FamiliarCatalog.ALL.get(i).key();
            out.add(ShowcasePlacement.label(x, z, ShowcasePlacement.Line.translated("showcase.herbalwitch.familiar." + species), ShowcasePlacement.Line.translated("showcase.herbalwitch.familiar.display")));
            for (int dx = 1; dx < 5; dx++) for (int dz = 1; dz < 5; dz++) out.add(ShowcasePlacement.floor(x + dx, z + dz, () -> Blocks.GRASS_BLOCK.defaultBlockState()));
        }
        for (int i = 0; i < 3; i++) {
            int supplies = i, x = i * 4;
            out.add(ShowcasePlacement.label(x, 18, ShowcasePlacement.Line.translated("showcase.herbalwitch.familiar.supplies." + i)));
            out.add(ShowcasePlacement.content(x + 1, 1, 18, () -> Blocks.BARREL.defaultBlockState(), block -> {
                if (block instanceof BarrelBlockEntity barrel) { int slot = 0; for (var item : supplies(supplies)) barrel.setItem(slot++, new ItemStack(item)); }
            }));
        }
        return out;
    }
    public static List<Item> supplies(int group) {
        var out = new LinkedHashSet<Item>();
        if (group == 0) { FamiliarEquipmentCatalog.ALL.forEach(s -> out.add(FamiliarItems.gear(s.key()))); out.addAll(CharmItems.all()); }
        else if (group == 1) {
            FamiliarPreparationCatalog.ALL.forEach(s -> out.add(AdvancedPreparations.item(s))); out.add(AdvancedPreparations.item(AdvancedProductCatalog.VENOM_OIL));
            out.add(PreparationItems.product(EverydayProductCatalog.HEALING_SALVE)); out.add(io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems.BOOK.get());
            for (String species : List.of("raven", "owl", "snake")) out.add(FamiliarItems.egg(species));
        } else {
            for (var action : FamiliarActionCatalog.ALL) for (var requirement : io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering.requirements(action)) out.add(requirement.item());
            for (var bait : io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait.values()) out.add(io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems.bait(bait));
        }
        if (out.size() > 27) throw new IllegalStateException("Familiar showcase supply barrel overflow");
        return List.copyOf(out);
    }
}
