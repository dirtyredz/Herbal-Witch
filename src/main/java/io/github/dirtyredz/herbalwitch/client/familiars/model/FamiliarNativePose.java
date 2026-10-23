package io.github.dirtyredz.herbalwitch.client.familiars.model;

import io.github.dirtyredz.herbalwitch.client.familiars.state.FamiliarVisualCache;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

/** Cosmetic rest/guard projection only. Native locomotion and every vanilla texture stay intact. */
public final class FamiliarNativePose {
    private FamiliarNativePose() {}
    public static void reset(ModelPart root) { root.getAllParts().forEach(ModelPart::resetPose); }
    public static void apply(ModelPart root,String species,LivingEntity entity) {
        var visual=FamiliarVisualCache.get(entity);if(visual==null)return;
        apply(root,species,visual.resting(),visual.command().equals("guard_witch")||visual.command().equals("protect_home"));
    }
    public static void apply(ModelPart root,String species,boolean resting,boolean guarding) {
        if(!resting && !guarding)return;
        root.getChild("head").xRot=resting?.35F:-.15F;
        if(!resting)return;
        switch(species) {
            case "cat" -> {root.getChild("tail1").yRot=.35F;root.getChild("tail2").yRot=.65F;}
            case "fox" -> root.getChild("body").getChild("tail").yRot=.55F;
            case "wolf" -> root.getChild("tail").xRot=.35F;
            case "rabbit" -> {root.getChild("left_ear").xRot=.9F;root.getChild("right_ear").xRot=.9F;}
            case "bat" -> {
                var body=root.getChild("body");body.xRot=0;
                var right=body.getChild("right_wing");var left=body.getChild("left_wing");
                right.yRot=-1.2F;left.yRot=1.2F;
                right.getChild("right_wing_tip").yRot=-1.7F;left.getChild("left_wing_tip").yRot=1.7F;
            }
            default -> throw new IllegalArgumentException("Unknown native familiar");
        }
    }
}
