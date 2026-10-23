package io.github.dirtyredz.herbalwitch.client.familiars.render;

import io.github.dirtyredz.herbalwitch.client.familiars.model.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.species.flying.FamiliarRaven;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public final class FamiliarRigRenderer<T extends Mob> extends MobRenderer<T, FamiliarRigModel<T>> {
    private final String species;
    public FamiliarRigRenderer(EntityRendererProvider.Context context, String species) {
        super(context, new FamiliarRigModel<>(species, FamiliarRigLoader.load(species)), .25F); this.species = species;
        var anchor = switch (species) {
            case "raven" -> new FamiliarEquipmentLayer.Attachment(2.5F, 1, 0, .25F);
            case "owl" -> new FamiliarEquipmentLayer.Attachment(1.5F, -1, -3.5F, .25F);
            case "snake" -> new FamiliarEquipmentLayer.Attachment(0, 0, 0, .25F);
            default -> throw new IllegalArgumentException("Unknown familiar rig");
        };
        addLayer(new FamiliarEquipmentLayer<>(this, model::equipmentTransform, anchor, context.getItemRenderer()));
    }
    @Override public ResourceLocation getTextureLocation(T entity) { return model.texture(entity instanceof FamiliarRaven raven && raven.appearance() == 1 ? "crow" : species); }
}
