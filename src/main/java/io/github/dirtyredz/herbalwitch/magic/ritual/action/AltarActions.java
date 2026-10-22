package io.github.dirtyredz.herbalwitch.magic.ritual.action;

import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import io.github.dirtyredz.herbalwitch.magic.ritual.network.AltarRequest;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.platform.network.RateLimit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Optional;

/**
 * The server's handling of altar requests: rate-limited per sender (five a second), refused before
 * any change unless they name the sender's open, still-valid altar menu and its session nonce and
 * were made against the rules in force; then dispatched to the one action they name. Costs,
 * inventories and results are always derived here, never read from the request.
 */
public final class AltarActions {
    public static final int MAX_REQUESTS = 5;
    public static final int WINDOW = 20;
    private static final RateLimit LIMIT = new RateLimit(MAX_REQUESTS, WINDOW);

    private AltarActions() {
    }

    /** The sender's altar menu, if the request names it exactly and it is still valid. */
    public static Optional<AltarMenu> validMenu(ServerPlayer sender, AltarRequest request) {
        if (!(sender.containerMenu instanceof AltarMenu menu) || menu.containerId != request.containerId() || menu.nonce() != request.session()
                || !menu.stillValid(sender)) {
            return Optional.empty();
        }
        return Optional.of(menu);
    }

    /** Returns the notice suffix it produced, or empty when the request was refused unseen. */
    public static Optional<String> handle(ServerPlayer sender, AltarRequest request) {
        if (sender.getServer() == null || !LIMIT.allow(sender.getUUID(), sender.getServer().getTickCount())) {
            return Optional.empty();
        }
        Optional<AltarMenu> menu = validMenu(sender, request);
        if (menu.isEmpty()) {
            return Optional.empty();
        }
        if (request.rules() != WitchRules.server().fingerprint()) {
            AltarSessionService.notice(sender, "rules_changed");
            return Optional.of("rules_changed");
        }
        String suffix = switch (request.op()) {
            case FOUND -> FoundingAction.begin(sender, menu.get());
            case INVITE -> request.target().map(t -> InitiationAction.invite(sender, menu.get(), t)).orElse("initiation.not_near");
            case WITHDRAW -> InitiationAction.withdraw(sender);
            case ACCEPT -> InitiationAction.accept(sender, menu.get(), request.offer());
            case DECLINE -> InitiationAction.decline(sender, request.offer());
            case MEDITATE -> MeditationAction.begin(sender, menu.get());
            case ATTUNE -> AttunementAction.begin(sender, menu.get());
            case BLESS -> GardenBlessingAction.begin(sender, menu.get(), request.place());
            case PROTECT -> HomeProtectionAction.begin(sender, menu.get(), request.place());
            case STOP -> AltarSessionService.of(sender.getUUID()).map(s -> {
                AltarSessionService.cancel(s, "stopped");
                return "cancel.stopped";
            }).orElse("idle");
        };
        AltarSessionService.notice(sender, suffix);
        return Optional.of(suffix);
    }

    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LIMIT.forget(event.getEntity().getUUID());
    }
}
