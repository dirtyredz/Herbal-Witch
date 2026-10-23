package io.github.dirtyredz.herbalwitch.client.familiars.model;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.dirtyredz.herbalwitch.client.familiars.state.FamiliarVisualCache;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public final class FamiliarRigModel<T extends Mob> extends HierarchicalModel<T> {
    private final String species;
    private final FamiliarRigLoader.Rig rig;
    public FamiliarRigModel(String species, FamiliarRigLoader.Rig rig) { this.species = species; this.rig = rig; }
    @Override public ModelPart root() { return rig.root(); }
    public ResourceLocation texture(String appearance) { return rig.appearances().getOrDefault(appearance, rig.appearances().get(species)); }
    public void equipmentTransform(PoseStack pose) { rig.equipmentPath().forEach(part -> part.translateAndRotate(pose)); }
    @Override public void setupAnim(T entity, float limb, float amount, float age, float yaw, float pitch) {
        root().getAllParts().forEach(ModelPart::resetPose);
        var visual = FamiliarVisualCache.get(entity);
        String pose = attackTime > 0 ? "attack" : !species.equals("snake") && !entity.onGround() ? "flight" : visual != null && visual.resting() ? "rest" : amount > .01 ? "walk" : "idle";
        FamiliarAnimations.apply(rig.bones(), species, pose, age);
        var head = rig.bones().get("head"); head.yRot += (float) Math.toRadians(yaw); head.xRot += (float) Math.toRadians(pitch);
    }
}
