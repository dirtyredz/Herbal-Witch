package io.github.dirtyredz.herbalwitch.client.familiars.render;

import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarEntities;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.*;
import net.minecraftforge.client.event.EntityRenderersEvent;

/** Native species retain the vanilla renderer, textures, variants and animation; only the equipment layer is added. */
public final class FamiliarNativeRenderers {
    private FamiliarNativeRenderers() {}
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FamiliarEntities.CAT.get(), Cat::new);
        event.registerEntityRenderer(FamiliarEntities.FOX.get(), Fox::new);
        event.registerEntityRenderer(FamiliarEntities.WOLF.get(), Wolf::new);
        event.registerEntityRenderer(FamiliarEntities.RABBIT.get(), Rabbit::new);
        event.registerEntityRenderer(FamiliarEntities.BAT.get(), Bat::new);
    }
    private static final class Cat extends CatRenderer {
        Cat(EntityRendererProvider.Context context) {
            super(context); var root = context.bakeLayer(ModelLayers.CAT); model = new CatModel<>(root);
            addLayer(new FamiliarEquipmentLayer<>(this, () -> root.getChild("head"), new FamiliarEquipmentLayer.Attachment(0, 1.5F, .5F, .2F), context.getItemRenderer()));
        }
    }
    private static final class Fox extends FoxRenderer {
        Fox(EntityRendererProvider.Context context) {
            super(context); var root = context.bakeLayer(ModelLayers.FOX); model = new FoxModel<>(root);
            addLayer(new FamiliarEquipmentLayer<>(this, () -> root.getChild("body"), new FamiliarEquipmentLayer.Attachment(3.5F, 2, 0, .3F), context.getItemRenderer()));
        }
    }
    private static final class Wolf extends WolfRenderer {
        Wolf(EntityRendererProvider.Context context) {
            super(context); var root = context.bakeLayer(ModelLayers.WOLF); model = new WolfModel<>(root);
            addLayer(new FamiliarEquipmentLayer<>(this, () -> root.getChild("upper_body"), new FamiliarEquipmentLayer.Attachment(0, 0, -1, .48F), context.getItemRenderer()));
        }
    }
    private static final class Rabbit extends RabbitRenderer {
        Rabbit(EntityRendererProvider.Context context) {
            super(context); var root = context.bakeLayer(ModelLayers.RABBIT); model = new RabbitModel<>(root);
            addLayer(new FamiliarEquipmentLayer<>(this, () -> root.getChild("body"), new FamiliarEquipmentLayer.Attachment(2.5F, 0, 0, .2F), context.getItemRenderer()));
        }
    }
    private static final class Bat extends BatRenderer {
        Bat(EntityRendererProvider.Context context) {
            super(context); var root = context.bakeLayer(ModelLayers.BAT); model = new BatModel(root);
            addLayer(new FamiliarEquipmentLayer<>(this, () -> root.getChild("body"), new FamiliarEquipmentLayer.Attachment(0, 3, -2, .2F), context.getItemRenderer()));
        }
    }
}
