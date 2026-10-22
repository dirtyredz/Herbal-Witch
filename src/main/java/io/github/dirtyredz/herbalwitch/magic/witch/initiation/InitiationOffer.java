package io.github.dirtyredz.herbalwitch.magic.witch.initiation;

import java.util.Optional;
import java.util.UUID;

/**
 * One pending, transient invitation to become a witch. It never survives a restart and is consumed
 * by acceptance, refusal, withdrawal, expiry or anything that invalidates it. An administrative
 * designation has no initiator and no site: the recipient accepts it at any valid altar, and it
 * waives the offering, the energy cost and the initiator's presence (never the recipient's consent).
 *
 * @param id          server-issued identity the acceptance must name
 * @param initiator   the inviting witch, empty for an administrative designation
 * @param recipient   who may accept
 * @param site        the altar the invitation was made at, empty for a designation
 * @param createdTick server tick of the invitation
 * @param expiresTick first server tick at which it is no longer valid
 */
public record InitiationOffer(long id, Optional<UUID> initiator, UUID recipient, Optional<RitualSite> site, long createdTick,
                              long expiresTick) {
    public boolean administrative() {
        return initiator.isEmpty();
    }

    public boolean expired(long now) {
        return now >= expiresTick;
    }

    public int ticksLeft(long now) {
        return (int) Math.max(0, expiresTick - now);
    }

    public boolean involves(UUID player) {
        return recipient.equals(player) || initiator.map(player::equals).orElse(false);
    }
}
