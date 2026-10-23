package io.github.dirtyredz.herbalwitch.client.familiars.render;

import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarEntities;
import io.github.dirtyredz.herbalwitch.client.familiars.model.FamiliarNativeModels;
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
            super(context); var root = context.bakeLayer(ModelLayers.CAT); model = FamiliarNativeModels.cat(root);
            var anchor = FamiliarNativeAttachment.of("cat");
            addLayer(new FamiliarEquipmentLayer<>(this, () -> root.getChild(anchor.bone()), anchor.transform(), context.getItemRenderer()));
        }
    }
    private static final class Fox extends FoxRenderer {
        Fox(EntityRendererProvider.Context context) {
            super(context); var root = context.bakeLayer(ModelLayers.FOX); model = FamiliarNativeModels.fox(root);
            var anchor = FamiliarNativeAttachment.of("fox");
            addLayer(new FamiliarEquipmentLayer<>(this, () -> root.getChild(anchor.bone()), anchor.transform(), context.getItemRenderer()));
        }
    }
    private static final class Wolf extends WolfRenderer {
        Wolf(EntityRendererProvider.Context context) {
            super(context); var root = context.bakeLayer(ModelLayers.WOLF); model = FamiliarNativeModels.wolf(root);
            var anchor = FamiliarNativeAttachment.of("wolf");
            addLayer(new FamiliarEquipmentLayer<>(this, () -> root.getChild(anchor.bone()), anchor.transform(), context.getItemRenderer()));
        }
    }
    private static final class Rabbit extends RabbitRenderer {
        Rabbit(EntityRendererProvider.Context context) {
            super(context); var root = context.bakeLayer(ModelLayers.RABBIT); model = FamiliarNativeModels.rabbit(root);
            var anchor = FamiliarNativeAttachment.of("rabbit");
            addLayer(new FamiliarEquipmentLayer<>(this, () -> root.getChild(anchor.bone()), anchor.transform(), context.getItemRenderer()));
        }
    }
    private static final class Bat extends BatRenderer {
        Bat(EntityRendererProvider.Context context) {
            super(context); var root = context.bakeLayer(ModelLayers.BAT); model = FamiliarNativeModels.bat(root);
            var anchor = FamiliarNativeAttachment.of("bat");
            addLayer(new FamiliarEquipmentLayer<>(this, () -> root.getChild(anchor.bone()), anchor.transform(), context.getItemRenderer()));
        }
    }
}
