package io.github.dirtyredz.herbalwitch.client.familiars.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.dirtyredz.herbalwitch.client.familiars.state.FamiliarVisualCache;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarItems;
import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmCatalog;
import io.github.dirtyredz.herbalwitch.magic.charm.registry.CharmItems;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import java.util.function.Supplier;

/** Gear follows the animated parent bone. Only public cosmetic item identities enter this layer. */
public final class FamiliarEquipmentLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public record Attachment(float x, float y, float z, float scale) {}
    private final ItemRenderer items;
    private final java.util.function.Consumer<PoseStack> anchor;
    private final Attachment attachment;
    public FamiliarEquipmentLayer(RenderLayerParent<T, M> parent, Supplier<ModelPart> anchor, Attachment attachment, ItemRenderer items) {
        this(parent, pose -> anchor.get().translateAndRotate(pose), attachment, items);
    }
    public FamiliarEquipmentLayer(RenderLayerParent<T, M> parent, java.util.function.Consumer<PoseStack> anchor, Attachment attachment, ItemRenderer items) {
        super(parent); this.anchor = anchor; this.attachment = attachment; this.items = items;
    }
    @Override public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity,
                                 float limb, float amount, float partial, float age, float yaw, float pitch) {
        var visual = FamiliarVisualCache.get(entity); if (visual == null || entity.isInvisible()) return;
        if (!visual.gear().isEmpty()) renderItem(pose, buffers, light, entity, new ItemStack(FamiliarItems.gear(visual.gear())), false);
        if (!visual.charm().isEmpty()) CharmCatalog.ALL.stream().filter(c -> c.id().equals(visual.charm())).findFirst()
                .ifPresent(charm -> renderItem(pose, buffers, light, entity, new ItemStack(CharmItems.item(charm)), true));
    }
    private void renderItem(PoseStack pose, MultiBufferSource buffers, int light, T entity, ItemStack stack, boolean charm) {
        pose.pushPose(); anchor.accept(pose);
        pose.translate(attachment.x() / 16, attachment.y() / 16, attachment.z() / 16);
        if (charm) pose.translate(attachment.scale() * .65, 0, 0);
        float scale = attachment.scale() * (charm ? .55F : 1);
        pose.scale(scale, -scale, -scale);
        items.renderStatic(stack, ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY, pose, buffers, entity.level(), entity.getId());
        pose.popPose();
    }
}
