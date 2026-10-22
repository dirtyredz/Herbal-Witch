package io.github.dirtyredz.herbalwitch.client.brewing;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.BrewingEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;

/**
 * Thrown preparations render like vanilla thrown items (the vial or dust the projectile carries); their
 * burst particles are sent by the server, bounded, at impact.
 */
public final class PreparationRenderers {
    private PreparationRenderers() {
    }

    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(BrewingEntities.PREPARATION_PROJECTILE.get(), ThrownItemRenderer::new);
    }
}
