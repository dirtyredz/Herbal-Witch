package io.github.dirtyredz.herbalwitch.magic.ritual.action;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.magic.witch.grimoire.WitchEntries;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery.DiscoveryService;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.magic.ritual.grimoire.AltarEntries;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSession;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService;
import io.github.dirtyredz.herbalwitch.magic.witch.status.EnergyService;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.founding.FoundingService;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchState;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * The one completion of a conversion (founding, initiation, administrative designation), on the
 * server thread. Everything is checked first (status, the offering's captured stacks, the payer's
 * energy, room for exactly one grimoire after the offering is gone, the founding claim); the session
 * is then marked consumed so a duplicate can never repeat it; a founding persists its durable claim;
 * then the offering is consumed, the energy debited, the status set and the grimoire given in one
 * step, rolled back from snapshots on any synchronous failure. Practice goes to a paying witch only
 * after success; discoveries are granted through the grimoire's discovery service (holding the
 * grimoire is never what makes a witch).
 */
public final class ConversionCommit {
    /** How a completion went, also the notice {@code ritual.herbalwitch.result.<id>}. */
    public enum Outcome {
        CONVERTED,
        ALREADY_WITCH,
        INPUTS_CHANGED,
        NOT_ENOUGH_ENERGY,
        NO_ROOM,
        CLAIM_LOST,
        NOT_PERSISTED,
        DUPLICATE,
        FAILED;

        public String suffix() {
            return "result." + name().toLowerCase(Locale.ROOT);
        }
    }

    private ConversionCommit() {
    }

    /**
     * @param payer        who supplies the offering and the energy (empty for a designation)
     * @param offering     the offering captured when the session began
     * @param energyUnits  the payer's energy cost
     * @param foundingClaim the world claim of a founding
     */
    public static Outcome commit(MinecraftServer server, AltarSession session, Optional<ServerPlayer> payer, ServerPlayer recipient,
                                 AltarOffering offering, int energyUnits, Optional<UUID> foundingClaim) {
        if (WitchStatusService.isWitch(recipient)) {
            return Outcome.ALREADY_WITCH;
        }
        if (payer.isPresent() && !offering.intact(payer.get())) {
            return Outcome.INPUTS_CHANGED;
        }
        if (payer.isPresent() && energyUnits > 0 && WitchStatusService.check(payer.get(), energyUnits) != EnergyService.Result.OK) {
            return Outcome.NOT_ENOUGH_ENERGY;
        }
        boolean sameInventory = payer.map(p -> p == recipient).orElse(false);
        if (!offering.giftFits(recipient, sameInventory)) {
            return Outcome.NO_ROOM;
        }
        if (foundingClaim.isPresent() && !FoundingService.holds(server, foundingClaim.get())) {
            return Outcome.CLAIM_LOST;
        }
        if (!AltarSessionService.completeOnce(session)) {
            return Outcome.DUPLICATE;
        }
        if (foundingClaim.isPresent() && !FoundingService.beginCommit(server, foundingClaim.get())) {
            return Outcome.NOT_PERSISTED;
        }
        Optional<WitchState> payerState = payer.flatMap(WitchStatusService::snapshot);
        Optional<WitchState> recipientState = WitchStatusService.snapshot(recipient);
        java.util.Map<Integer, ItemStack> payerSlots = payer.map(offering::backup).orElse(java.util.Map.of());
        List<ItemStack> recipientItems = copy(recipient.getInventory());
        try {
            payer.ifPresent(offering::consume);
            if (payer.isPresent() && energyUnits > 0 && WitchStatusService.debit(payer.get(), energyUnits) != EnergyService.Result.OK) {
                throw new IllegalStateException("debit refused after its check");
            }
            if (!WitchStatusService.convert(recipient, UUID.randomUUID())) {
                throw new IllegalStateException("conversion refused after its check");
            }
            if (!recipient.getInventory().add(new ItemStack(GrimoireItems.BOOK.get()))) {
                throw new IllegalStateException("the grimoire did not fit after its check");
            }
        } catch (RuntimeException e) {
            payer.ifPresent(p -> AltarOffering.restore(p, payerSlots));
            restore(recipient.getInventory(), recipientItems);
            payer.ifPresent(p -> payerState.ifPresent(s -> WitchStatusService.restore(p, s)));
            recipientState.ifPresent(s -> WitchStatusService.restore(recipient, s));
            foundingClaim.ifPresent(claim -> FoundingService.abortCommit(server, claim));
            return Outcome.FAILED;
        }
        foundingClaim.ifPresent(claim -> FoundingService.complete(server, claim, recipient.getUUID()));
        payer.ifPresent(p -> {
            if (energyUnits > 0) {
                WitchStatusService.awardPractice(p, energyUnits);
            }
            p.getInventory().setChanged();
            WitchStatusService.flush(p);
        });
        List<EntryKey> learned = new ArrayList<>();
        WitchEntries.PAGES.forEach(page -> learned.add(WitchEntries.key(page)));
        learned.add(AltarEntries.key(session.action()));
        DiscoveryService.grantAll(recipient, learned);
        payer.filter(p -> p != recipient).ifPresent(p -> DiscoveryService.grant(p, AltarEntries.key(session.action())));
        WitchStatusService.flush(recipient);
        return Outcome.CONVERTED;
    }

    private static List<ItemStack> copy(Inventory inventory) {
        List<ItemStack> out = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            out.add(inventory.getItem(i).copy());
        }
        return out;
    }

    private static void restore(Inventory inventory, List<ItemStack> items) {
        for (int i = 0; i < items.size(); i++) {
            inventory.setItem(i, items.get(i));
        }
    }
}
