package io.github.dirtyredz.herbalwitch.client.familiars.render;

import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;

public final class FamiliarRenderers {
    private FamiliarRenderers() {}
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FamiliarEntities.RAVEN.get(), context -> new FamiliarRigRenderer<>(context, "raven"));
        event.registerEntityRenderer(FamiliarEntities.OWL.get(), context -> new FamiliarRigRenderer<>(context, "owl"));
        event.registerEntityRenderer(FamiliarEntities.SNAKE.get(), context -> new FamiliarRigRenderer<>(context, "snake"));
    }
}
