package io.github.dirtyredz.herbalwitch.client.witch.hud;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.client.witch.WitchClientConfig;
import io.github.dirtyredz.herbalwitch.client.witch.WitchClientState;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/**
 * The energy HUD, client only: drawn for a local witch whose status has arrived, hidden for
 * spectators and when the GUI is hidden. A framed bar (from {@code textures/gui/witch/energy.png})
 * with a smoothly easing fill that never moves past the last received value, an unmistakable cracked
 * empty state at zero, the numbers above it, and a small rest or meditation icon while energy is
 * recovering. Where it sits is {@link EnergyHudLayout}'s arithmetic over the client settings.
 */
public final class EnergyOverlay implements IGuiOverlay {
    public static final String ID = "witch_energy";
    public static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "textures/gui/witch/energy.png");
    private static final int FILL_WIDTH = 78;
    private static final int FILL_HEIGHT = 6;
    private float displayed = -1;

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (!WitchClientConfig.ENABLED.get() || mc.options.hideGui || mc.player == null || mc.player.isSpectator()) {
            return;
        }
        WitchSnapshot s = WitchClientState.INSTANCE.snapshot().filter(WitchSnapshot::initiated).orElse(null);
        if (s == null) {
            displayed = -1;
            return;
        }
        boolean numeric = WitchClientConfig.NUMERIC.get();
        boolean topLine = numeric || WitchClientConfig.RECOVERY_INDICATOR.get();
        EnergyHudLayout.Placement at = EnergyHudLayout.place(WitchClientConfig.ANCHOR.get(), WitchClientConfig.OFFSET_X.get(),
                WitchClientConfig.OFFSET_Y.get(), WitchClientConfig.SCALE.get(), screenWidth, screenHeight, EnergyHudLayout.BAR_WIDTH,
                EnergyHudLayout.height(topLine));
        int target = EnergyHudLayout.fill(s.energy(), s.capacity(), FILL_WIDTH);
        displayed = displayed < 0 ? target : EnergyHudLayout.ease(displayed, target, 0.25F);
        graphics.pose().pushPose();
        graphics.pose().translate(at.x(), at.y(), 0);
        graphics.pose().scale(at.scale(), at.scale(), 1);
        int barY = topLine ? EnergyHudLayout.TEXT_HEIGHT : 0;
        if (numeric) {
            Component text = Component.translatable("hud.herbalwitch.energy", s.energy() / EnergyBalance.UNITS,
                    s.capacity() / EnergyBalance.UNITS);
            graphics.drawString(mc.font, text, 1, 0, s.energy() == 0 ? 0xFFE06A8C : 0xFFF2E8CF, true);
        }
        graphics.blit(ATLAS, 0, barY, 0, 0, EnergyHudLayout.BAR_WIDTH, EnergyHudLayout.BAR_HEIGHT);
        int fill = Math.round(displayed);
        if (fill > 0) {
            graphics.blit(ATLAS, 2, barY + 2, 0, 12, fill, FILL_HEIGHT);
        } else {
            graphics.blit(ATLAS, 2, barY + 2, 0, 20, FILL_WIDTH, FILL_HEIGHT); // the cracked empty state
        }
        if (WitchClientConfig.RECOVERY_INDICATOR.get() && s.recovering() != WitchSnapshot.Recovering.NONE) {
            int u = s.recovering() == WitchSnapshot.Recovering.MEDITATION ? 94 : 84;
            graphics.blit(ATLAS, EnergyHudLayout.BAR_WIDTH - 9, 0, u, 0, 9, 9);
        }
        graphics.pose().popPose();
    }

    /** Disconnect or replacement: the next value is shown at once, never eased from the old one. */
    public void reset() {
        displayed = -1;
    }
}
