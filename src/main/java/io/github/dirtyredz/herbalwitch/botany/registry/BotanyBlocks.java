package io.github.dirtyredz.herbalwitch.botany.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.botany.crop.HerbBushBlock;
import io.github.dirtyredz.herbalwitch.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.botany.garden.HerbBedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Herb plants (one per catalogue herb, {@code botany/<group>/<herb>/plant}) and the herb bed. */
public final class BotanyBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HerbalWitch.MOD_ID);

    private static final Map<HerbSpec, RegistryObject<Block>> PLANTS = new LinkedHashMap<>();

    public static final String HERB_BED_PATH = "botany/garden/herb_bed";
    public static final RegistryObject<Block> HERB_BED = BLOCKS.register(HERB_BED_PATH, () -> new HerbBedBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(1.0F).sound(SoundType.WOOD).randomTicks()
                    .noOcclusion().ignitedByLava()));

    static {
        for (HerbSpec herb : HerbCatalog.ALL) {
            PLANTS.put(herb, BLOCKS.register(herb.plantPath(), () -> createPlant(herb)));
        }
    }

    private BotanyBlocks() {
    }

    private static Block createPlant(HerbSpec herb) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission()
                .randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY);
        return herb.form() == HerbSpec.Form.BUSH
                ? new HerbBushBlock(properties, herb, () -> BotanyItems.seeds(herb))
                : new HerbCropBlock(properties, herb, () -> BotanyItems.seeds(herb));
    }

    public static HerbCropBlock plant(HerbSpec herb) {
        return (HerbCropBlock) PLANTS.get(herb).get();
    }

    public static RegistryObject<Block> plantObject(HerbSpec herb) {
        return PLANTS.get(herb);
    }

    /** Every herb plant, in catalogue order. */
    public static List<HerbCropBlock> plants() {
        return HerbCatalog.ALL.stream().map(BotanyBlocks::plant).toList();
    }

    public static HerbBedBlock herbBed() {
        return (HerbBedBlock) HERB_BED.get();
    }
}
