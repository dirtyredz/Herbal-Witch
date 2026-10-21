package io.github.dirtyredz.herbalwitch.data.client.decor;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Inventory models: every item is its piece's template (the unlit lamp, the north-facing piece), so
 * item, placed block and dropped item look alike. Display transforms follow vanilla's block item
 * defaults, with the flat rug and the wall shelf shown a little larger.
 */
public class DecorItemModelProvider extends ItemModelProvider {
    public DecorItemModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch decor item models";
    }

    @Override
    protected void registerModels() {
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            FurnitureSpec spec = entry.spec();
            ItemModelBuilder item = getBuilder("item/" + spec.path())
                    .parent(new ModelFile.UncheckedModelFile(modLoc(DecorModelProvider.itemTemplate(spec))));
            float gui = guiScale(spec);
            display(item, ItemDisplayContext.GUI, 30, 225, 0, 0, 0, 0, gui);
            display(item, ItemDisplayContext.GROUND, 0, 0, 0, 0, 3, 0, 0.25F);
            display(item, ItemDisplayContext.FIXED, 0, 0, 0, 0, 0, 0, 0.5F);
            display(item, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, 75, 45, 0, 0, 2.5F, 0, 0.375F);
            display(item, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, 0, 45, 0, 0, 0, 0, 0.4F);
            display(item, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, 0, 225, 0, 0, 0, 0, 0.4F);
        }
    }

    /** Vanilla blocks use 0.625; thin pieces would otherwise look lost in the slot. */
    static float guiScale(FurnitureSpec spec) {
        return switch (spec.behavior()) {
            case RUG, WALL_MOUNTED -> 0.75F;
            default -> 0.625F;
        };
    }

    private static void display(ItemModelBuilder builder, ItemDisplayContext context, float rx, float ry, float rz,
                                float tx, float ty, float tz, float scale) {
        builder.transforms().transform(context)
                .rotation(rx, ry, rz)
                .translation(tx, ty, tz)
                .scale(scale)
                .end().end();
    }
}
