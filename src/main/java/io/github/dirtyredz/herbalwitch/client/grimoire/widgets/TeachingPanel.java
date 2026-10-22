package io.github.dirtyredz.herbalwitch.client.grimoire.widgets;

import io.github.dirtyredz.herbalwitch.client.grimoire.GrimoireClientState;
import io.github.dirtyredz.herbalwitch.client.grimoire.GrimoireView;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import io.github.dirtyredz.herbalwitch.grimoire.network.GrimoirePackets;
import io.github.dirtyredz.herbalwitch.grimoire.network.ReaderUpdate;
import io.github.dirtyredz.herbalwitch.grimoire.network.TeachingRequest;
import io.github.dirtyredz.herbalwitch.grimoire.network.TeachingUpdate;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Optional;

/**
 * The consent interface. An invitation addressed to this player shows as a banner on every reader
 * page with Accept and Decline (nothing happens until the player chooses). At a research block, a
 * known recipe's page offers it to a nearby player, or shows the pending offer with Withdraw.
 */
public final class TeachingPanel {
    private final GrimoireView view;
    private final Font font;
    private int chosen;

    public TeachingPanel(GrimoireView view, Font font) {
        this.view = view;
        this.font = font;
    }

    /** The incoming invitation banner; returns the height it took (0 without one). */
    public int renderIncoming(GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY, List<TextButton> buttons) {
        Optional<TeachingUpdate> incoming = GrimoireClientState.INSTANCE.incoming();
        if (incoming.isEmpty() || view.reader().isEmpty() || incoming.get().recipe().isEmpty()) {
            return 0;
        }
        TeachingUpdate invite = incoming.get();
        Component recipe = view.content().get(invite.recipe().get()).map(GrimoireEntry::title)
                .orElse(Component.literal(invite.recipe().get().id()));
        List<FormattedCharSequence> lines = font.split(view.text("invited", invite.otherName(), recipe), width - 4);
        int h = lines.size() * 9 + TextButton.HEIGHT + 6;
        graphics.fill(x, y, x + width, y + h, 0xFF4A2A55);
        graphics.fill(x + 1, y + 1, x + width - 1, y + h - 1, 0xFFF2E8CF);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(font, lines.get(i), x + 3, y + 3 + i * 9, TextButton.PLUM, false);
        }
        int by = y + 3 + lines.size() * 9;
        TextButton accept = view.button(x + 3, by, view.text("accept"), true, () -> send(TeachingRequest.Op.ACCEPT, invite));
        TextButton decline = view.button(x + 6 + accept.width(), by, view.text("decline"), true,
                () -> send(TeachingRequest.Op.DECLINE, invite));
        buttons.add(accept);
        buttons.add(decline);
        accept.render(graphics, font, mouseX, mouseY);
        decline.render(graphics, font, mouseX, mouseY);
        return h + 3;
    }

    /** At a research block, under a known recipe: offer it, or the pending offer. Returns the next y. */
    public int renderInvite(GuiGraphics graphics, GrimoireEntry entry, int x, int cy, int width, int mouseX, int mouseY,
                            List<TextButton> buttons) {
        if (view.reader().map(r -> !r.access().atResearchBlock()).orElse(true)) {
            return cy;
        }
        Optional<TeachingUpdate> outgoing = GrimoireClientState.INSTANCE.outgoing();
        graphics.drawString(font, view.text("teach"), x, cy, TextButton.PLUM, false);
        cy += 10;
        if (outgoing.isPresent()) {
            Component recipe = outgoing.get().recipe().flatMap(k -> view.content().get(k)).map(GrimoireEntry::title)
                    .orElse(Component.empty());
            for (FormattedCharSequence line : font.split(view.text("offered", outgoing.get().otherName(), recipe), width - 2)) {
                graphics.drawString(font, line, x, cy, TextButton.INK, false);
                cy += 9;
            }
            TextButton withdraw = view.button(x, cy + 1, view.text("withdraw"), true, () -> send(TeachingRequest.Op.CANCEL, null));
            buttons.add(withdraw);
            withdraw.render(graphics, font, mouseX, mouseY);
            return cy + TextButton.HEIGHT + 4;
        }
        List<ReaderUpdate.Nearby> nearby = GrimoireClientState.INSTANCE.nearby();
        if (nearby.isEmpty()) {
            graphics.drawWordWrap(font, view.text("nobody"), x, cy, width - 2, TextButton.FAINT);
            return cy + font.split(view.text("nobody"), width - 2).size() * 9 + 2;
        }
        chosen = Math.floorMod(chosen, nearby.size());
        ReaderUpdate.Nearby target = nearby.get(chosen);
        TextButton who = new TextButton(x, cy, Math.min(width - 40, font.width(target.name()) + 10),
                Component.literal(target.name()), nearby.size() > 1, () -> chosen++);
        TextButton invite = view.button(x + who.width() + 3, cy, view.text("invite"), true, () -> {
            view.reader().ifPresent(r -> GrimoirePackets.sendToServer(new TeachingRequest(r.containerId(), r.nonce(),
                    GrimoireClientState.INSTANCE.revision(), TeachingRequest.Op.INVITE, Optional.of(entry.key()),
                    Optional.of(target.id()))));
        });
        buttons.add(who);
        buttons.add(invite);
        who.render(graphics, font, mouseX, mouseY);
        invite.render(graphics, font, mouseX, mouseY);
        return cy + TextButton.HEIGHT + 4;
    }

    private void send(TeachingRequest.Op op, TeachingUpdate invite) {
        view.reader().ifPresent(r -> GrimoirePackets.sendToServer(new TeachingRequest(r.containerId(), r.nonce(),
                GrimoireClientState.INSTANCE.revision(), op, invite == null ? Optional.empty() : invite.recipe(),
                invite == null ? Optional.empty() : invite.other())));
    }
}
