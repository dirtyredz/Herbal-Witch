package io.github.dirtyredz.herbalwitch.client.magic.ward;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.dirtyredz.herbalwitch.magic.ward.menu.WardMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * After a ward's screen closes, its boundary (the box the server uses: radius horizontally, height
 * vertically, as the screen last showed them) is outlined for {@link #TICKS} ticks. Local and advisory:
 * one box, nothing sent, nothing decided here.
 */
public final class WardBoundaryPreview {
    public static final int TICKS = 200;
    private static AABB box;
    private static long until;

    private WardBoundaryPreview() {
    }

    /** Client setup: the screen and the preview. */
    public static void register(IEventBus forge, net.minecraft.world.inventory.MenuType<WardMenu> menu) {
        MenuScreens.register(menu, WardScreen::new);
        forge.addListener(WardBoundaryPreview::render);
        forge.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> box = null);
    }

    public static void show(BlockPos stone, int radius, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        box = new AABB(stone.getX() - radius, stone.getY() - height, stone.getZ() - radius, stone.getX() + radius + 1,
                stone.getY() + height + 1, stone.getZ() + radius + 1);
        until = mc.level.getGameTime() + TICKS;
    }

    private static void render(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (box == null || mc.level == null || event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (mc.level.getGameTime() > until) {
            box = null;
            return;
        }
        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(pose, lines, box.move(-camera.x, -camera.y, -camera.z), 0.85F, 0.35F, 0.6F, 0.9F);
        buffers.endBatch(RenderType.lines());
    }
}
