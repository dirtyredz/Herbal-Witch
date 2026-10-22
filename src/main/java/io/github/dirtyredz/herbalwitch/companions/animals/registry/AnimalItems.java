package io.github.dirtyredz.herbalwitch.companions.animals.registry;

import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalCareItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class AnimalItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "herbalwitch");
    private static final Map<Bait, RegistryObject<Item>> BAITS = new EnumMap<>(Bait.class);
    static { for (Bait bait : Bait.values()) BAITS.put(bait, ITEMS.register(bait.itemPath(), () -> new AnimalCareItem(bait))); }
    public static final RegistryObject<Item> CALMING = ITEMS.register("animals/care/calming_powder", () -> new AnimalCareItem(null));
    public static final RegistryObject<Item> CHARM = ITEMS.register("animals/capture/charm", () -> new io.github.dirtyredz.herbalwitch.companions.animals.capture.CaptureCharmItem(false));
    public static final RegistryObject<Item> FILLED_CHARM = ITEMS.register("animals/capture/filled_charm", () -> new io.github.dirtyredz.herbalwitch.companions.animals.capture.CaptureCharmItem(true));
    private AnimalItems() {}
    public static Item bait(Bait bait) { return BAITS.get(bait).get(); }
    public static List<Item> all() { return Stream.concat(BAITS.values().stream(), Stream.of(CALMING, CHARM)).map(RegistryObject::get).toList(); }
}
