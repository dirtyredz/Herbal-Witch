package io.github.dirtyredz.herbalwitch.client.familiars.render;

import io.github.dirtyredz.herbalwitch.client.familiars.state.FamiliarObservationCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.RenderLevelStageEvent;

/** Only private server-issued marks are drawn, at their observed position, for five seconds. */
public final class FamiliarObservationRenderer {
    private FamiliarObservationRenderer() {}
    public static void render(RenderLevelStageEvent event) {
        var mc = Minecraft.getInstance();
        if (mc.level == null || event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        var marks = FamiliarObservationCache.visible(); if (marks.isEmpty()) return;
        var camera = event.getCamera().getPosition(); var buffers = mc.renderBuffers().bufferSource();
        var lines = buffers.getBuffer(RenderType.lines());
        for (var mark : marks) {
            var pos = BlockPos.of(mark.position()); if (!mc.level.hasChunkAt(pos)) continue;
            var box = new AABB(pos).inflate(.04).move(-camera.x, -camera.y, -camera.z);
            boolean herb = mark.kind().equals("herb");
            LevelRenderer.renderLineBox(event.getPoseStack(), lines, box, herb ? .4F : .8F, herb ? .8F : .4F, .65F, .9F);
        }
        buffers.endBatch(RenderType.lines());
    }
}
