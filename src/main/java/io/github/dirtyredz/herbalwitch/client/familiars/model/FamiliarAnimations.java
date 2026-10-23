package io.github.dirtyredz.herbalwitch.client.familiars.model;

import net.minecraft.client.model.geom.ModelPart;
import java.util.Map;

/** Procedural poses shared in contract with tools/entities/poses.mjs; authored angles are Y-up degrees. */
public final class FamiliarAnimations {
    private FamiliarAnimations() {}
    public static void apply(Map<String, ModelPart> bones, String species, String pose, float time) {
        rotate(bones, "head", 0, Math.sin(time * .08) * 4, 0);
        if (species.equals("snake")) {
            if (pose.equals("walk")) { rotate(bones, "body", 0, Math.sin(time * .5) * 6, 0); rotate(bones, "middle", 0, Math.sin(time * .5 + .8) * 12, 0); rotate(bones, "tail", 0, Math.sin(time * .5 + 1.6) * 18, 0); }
            if (pose.equals("rest")) { rotate(bones, "middle", 0, 35, 0); rotate(bones, "tail", 0, 35, 0); }
            if (pose.equals("attack")) { rotate(bones, "head", -18, 0, 0); rotate(bones, "jaw", 25, 0, 0); }
        } else {
            if (pose.equals("walk")) { rotate(bones, "leg_left", Math.sin(time * .7) * 25, 0, 0); rotate(bones, "leg_right", -Math.sin(time * .7) * 25, 0, 0); }
            if (pose.equals("flight")) {
                double flap = 50 + Math.sin(time * .7) * 25;
                rotate(bones, "wing_left", 0, 0, -flap); rotate(bones, "wing_right", 0, 0, flap); rotate(bones, "leg_left", -30, 0, 0); rotate(bones, "leg_right", -30, 0, 0);
            }
            if (pose.equals("rest")) { rotate(bones, "head", -8, 0, 0); rotate(bones, "wing_left", 0, 0, -4); rotate(bones, "wing_right", 0, 0, 4); }
            if (pose.equals("attack")) { rotate(bones, "head", -18, 0, 0); rotate(bones, "tail", 10, 0, 0); }
        }
    }
    private static void rotate(Map<String, ModelPart> bones, String name, double x, double y, double z) {
        var part = bones.get(name); if (part == null) return;
        part.xRot += (float) Math.toRadians(-x); part.yRot += (float) Math.toRadians(y); part.zRot += (float) Math.toRadians(-z);
    }
}
