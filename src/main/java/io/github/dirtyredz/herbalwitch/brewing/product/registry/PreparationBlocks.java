package io.github.dirtyredz.herbalwitch.brewing.product.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.brewing.product.block.DriedBundleBlock;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.HerbalMaterialCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.lighting.OilLanternBlock;
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

/** One hanging bundle block per herb (its item is the bundle) and the oil lantern. */
public final class PreparationBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HerbalWitch.MOD_ID);
    private static final Map<HerbSpec, RegistryObject<Block>> BUNDLES = new LinkedHashMap<>();

    public static final String OIL_LANTERN_PATH = "preparations/household/oil_lantern";
    public static final RegistryObject<Block> OIL_LANTERN = BLOCKS.register(OIL_LANTERN_PATH, () -> new OilLanternBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0F).sound(SoundType.LANTERN).noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    static {
        for (HerbSpec herb : HerbCatalog.ALL) {
            BUNDLES.put(herb, BLOCKS.register(HerbalMaterialCatalog.bundlePath(herb), () -> new DriedBundleBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.2F).sound(SoundType.AZALEA_LEAVES)
                            .noCollission().noOcclusion().pushReaction(PushReaction.DESTROY).ignitedByLava(), herb)));
        }
    }

    private PreparationBlocks() {
    }

    public static Block bundle(HerbSpec herb) {
        return BUNDLES.get(herb).get();
    }

    public static List<Block> bundles() {
        return HerbCatalog.ALL.stream().map(PreparationBlocks::bundle).toList();
    }
}
