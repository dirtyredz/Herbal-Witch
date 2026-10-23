package io.github.dirtyredz.herbalwitch.client.magic.ritual;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.client.grimoire.GrimoirePanelHost;
import io.github.dirtyredz.herbalwitch.client.magic.ritual.widgets.AltarActionPanel;
import io.github.dirtyredz.herbalwitch.client.magic.ritual.widgets.CirclePanel;
import io.github.dirtyredz.herbalwitch.client.magic.ritual.widgets.InitiationPanel;
import io.github.dirtyredz.herbalwitch.client.magic.witch.WitchClientState;
import io.github.dirtyredz.herbalwitch.magic.ritual.grimoire.AltarEntries;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import io.github.dirtyredz.herbalwitch.magic.ritual.network.AltarPackets;
import io.github.dirtyredz.herbalwitch.magic.ritual.network.AltarRequest;
import io.github.dirtyredz.herbalwitch.magic.ritual.network.AltarUpdate;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;
import io.github.dirtyredz.herbalwitch.magic.witch.network.WitchSnapshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The altar screen (252 x 200 from {@code textures/gui/witch/altar.png}), with no slots: the
 * player's own status and energy on the left with the altar's actions (founding for a non-witch,
 * meditation and attunement for a witch), consent on the right (an incoming invitation, or a witch's
 * nearby list and invitation; a witch can switch it to the ritual circle's two rituals), the running ritual's progress across the bottom, and the last result
 * or interruption. Everything shown comes from the server's private updates and snapshot; buttons
 * send intent only. The grimoire opens beside it on the altar's page.
 */
public class AltarScreen extends GrimoirePanelHost<AltarMenu> implements CirclePanel.CircleUi, io.github.dirtyredz.herbalwitch.client.magic.ritual.widgets.ContributedRitualPanel.Ui {
    public static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "textures/gui/witch/altar.png");
    private static final String UI = "ritual.herbalwitch.ui.";
    private static final int INK = 0xFF3B2416;
    private static final int COLUMN = 114;

    private record Line(FormattedCharSequence text, int x, int y, int color) {
    }

    private final List<Line> lines = new ArrayList<>();
    private final AltarActionPanel actions = new AltarActionPanel();
    private final InitiationPanel initiation = new InitiationPanel();
    private final CirclePanel circle = new CirclePanel();
    private int ritualView;
    private final io.github.dirtyredz.herbalwitch.client.magic.ritual.widgets.ContributedRitualPanel contributed = new io.github.dirtyredz.herbalwitch.client.magic.ritual.widgets.ContributedRitualPanel();
    private int built = -1;
    private Optional<AltarUpdate.Activity> activity = Optional.empty();

    public AltarScreen(AltarMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, Optional.empty(), Optional.of(AltarEntries.ALTAR), false);
        imageWidth = 252;
        imageHeight = 200;
    }

    @Override
    protected void init() {
        super.init();
        built = -1;
    }

    private int signature() {
        return AltarScreens.generation() * 31 + WitchClientState.INSTANCE.snapshot().map(WitchSnapshot::revision).orElse(-1);
    }

    /** Rebuilds every line and button from the latest server state. */
    private void rebuild() {
        clearWidgets();
        lines.clear();
        initGrimoire();
        int left = leftPos + 8;
        int right = leftPos + 130;
        int top = topPos + 18;
        Optional<WitchSnapshot> status = WitchClientState.INSTANCE.snapshot();
        boolean witch = status.map(WitchSnapshot::initiated).orElse(false);
        Component statusLine = witch ? Component.translatable(UI + "status.witch", status.get().energy() / EnergyBalance.UNITS,
                status.get().capacity() / EnergyBalance.UNITS) : Component.translatable(UI + "status.not_witch");
        int y = top + text(statusLine, left, top, COLUMN, witch && status.get().energy() == 0 ? 0xFFB0305A : INK) + 4;
        Optional<AltarUpdate> update = AltarScreens.latest(menu.containerId, menu.nonce());
        activity = update.flatMap(AltarUpdate::activity);
        if (update.isEmpty()) {
            text(Component.translatable(UI + "reading"), left, y, COLUMN, INK);
            return;
        }
        AltarUpdate u = update.get();
        if (activity.isPresent()) {
            AltarUpdate.Activity a = activity.get();
            Component what = Component.translatable("grimoire.herbalwitch.ritual." + a.action().key() + ".title");
            int h = text(a.with().isEmpty() ? Component.translatable(UI + "running", what) : Component.translatable(UI + "running_with", what, a.with()),
                    left, y, 236, INK);
            if (a.mine()) {
                button(Component.translatable(UI + "stop"), left, y + h + 14, 80, true, () -> send(AltarRequest.Op.STOP, Optional.empty(), 0));
            }
        } else if (witch) {
            actions.witch(this, u, status.get(), left, y, COLUMN);
            button(Component.translatable(UI + (ritualView == 0 ? "view_circle" : ritualView == 1 ? "view_contributed" : "view_initiation")), right, top, COLUMN, true, () -> {
                ritualView = (ritualView + 1) % 3;
                refresh();
            });
            if (ritualView == 2) {
                contributed.draw(this, u, status.get(), right, top + 18, COLUMN);
            } else if (ritualView == 1) {
                circle.witch(this, u, status.get(), right, top + 18, COLUMN);
            } else {
                initiation.initiator(this, u, status.get(), right, top + 18, COLUMN);
            }
        } else {
            int h = initiation.recipient(this, u, right, top, COLUMN);
            if (h == 0) {
                text(Component.translatable(UI + "no_invitation"), right, top, COLUMN, INK);
            }
            actions.founding(this, u, left, y, COLUMN);
        }
        u.notice().ifPresent(n -> text(Component.translatable(n), left, topPos + imageHeight - 18, 236, 0xFF4A2A55));
    }

    // ---------------------------------------------------------------- AltarUi
    @Override public void contributed(io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec action, UUID target) {
        AltarPackets.sendToServer(new AltarRequest(menu.containerId, menu.nonce(), WitchRules.current().fingerprint(), AltarRequest.Op.CONTRIBUTED,
                Optional.of(target), 0, Optional.empty(), action.identity()));
    }

    @Override
    public int text(Component text, int x, int y, int width, int color) {
        List<FormattedCharSequence> split = font.split(text, width);
        for (int i = 0; i < split.size(); i++) {
            lines.add(new Line(split.get(i), x, y + i * LINE, color));
        }
        return split.size() * LINE;
    }

    @Override
    public void button(Component label, int x, int y, int width, boolean active, Runnable onPress) {
        Button b = Button.builder(label, btn -> onPress.run()).bounds(x, y, width, 14).build();
        b.active = active;
        addRenderableWidget(b);
    }

    @Override
    public void send(AltarRequest.Op op, Optional<UUID> target, long offer) {
        AltarPackets.sendToServer(new AltarRequest(menu.containerId, menu.nonce(), WitchRules.current().fingerprint(), op, target, offer));
    }

    @Override
    public void send(AltarRequest.Op op, Optional<BlockPos> place) {
        AltarPackets.sendToServer(new AltarRequest(menu.containerId, menu.nonce(), WitchRules.current().fingerprint(), op, Optional.empty(), 0,
                place));
    }

    @Override
    public void refresh() {
        built = -1;
    }

    // ---------------------------------------------------------------- screen

    @Override
    protected void containerTick() {
        super.containerTick();
        if (built != signature()) {
            built = signature();
            rebuild();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (built == -1) {
            built = signature();
            rebuild();
        }
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        for (Line line : lines) {
            graphics.drawString(font, line.text(), line.x(), line.y(), line.color(), false);
        }
        renderGrimoire(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(ATLAS, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        activity.ifPresent(a -> {
            int barX = leftPos + 8;
            int barY = topPos + imageHeight - 34;
            graphics.blit(ATLAS, barX, barY, 0, 204, 120, 8);
            int fill = a.duration() > 0 ? Math.min(116, a.elapsed() * 116 / a.duration()) : (a.elapsed() / 2) % 117;
            graphics.blit(ATLAS, barX + 2, barY + 2, 0, 214, fill, 4);
        });
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, INK, false);
    }

}
