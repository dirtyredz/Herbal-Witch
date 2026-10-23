package io.github.dirtyredz.herbalwitch.companions.familiars.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationCreativeTab;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarEquipmentCatalog;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
import java.util.*;

public final class FamiliarItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HerbalWitch.MOD_ID);
    private static final Map<String, RegistryObject<Item>> GEAR = new LinkedHashMap<>();
    private static final Map<String, RegistryObject<Item>> EGGS = new LinkedHashMap<>();
    static { for (var spec : FamiliarEquipmentCatalog.ALL) GEAR.put(spec.key(), ITEMS.register(spec.path(), () -> new Item(new Item.Properties().stacksTo(1)))); }
    static {
        for (String species : List.of("raven", "owl", "snake")) EGGS.put(species, ITEMS.register("familiars/spawn_egg/" + species,
                () -> new net.minecraftforge.common.ForgeSpawnEggItem(() -> FamiliarEntities.type(species), 0x554634, 0x68784B, new Item.Properties())));
    }
    private FamiliarItems() {}
    public static Item gear(String key) { return Objects.requireNonNull(GEAR.get(key), "Unknown familiar equipment").get(); }
    public static Item egg(String species) { return Objects.requireNonNull(EGGS.get(species), "Unknown wild familiar").get(); }
    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        bus.addListener((net.minecraftforge.event.BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey().equals(StationCreativeTab.APOTHECARY.getKey())) GEAR.values().forEach(item -> event.accept(item.get()));
            if (event.getTabKey().equals(StationCreativeTab.APOTHECARY.getKey())) EGGS.values().forEach(item -> event.accept(item.get()));
        });
    }
}
