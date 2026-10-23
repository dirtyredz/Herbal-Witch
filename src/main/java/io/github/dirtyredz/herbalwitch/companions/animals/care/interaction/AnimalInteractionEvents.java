package io.github.dirtyredz.herbalwitch.companions.animals.care.interaction;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.companions.animals.inspection.menu.AnimalMenu;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalEligibility;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.platform.network.RateLimit;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** One click path only (not EntityInteractSpecific). Terminal replies prevent accidental offhand/self-use. */
public final class AnimalInteractionEvents {
    private static final RateLimit REQUESTS = new RateLimit(5, 20);
    private static final Map<UUID, Long> USED = new HashMap<>();
    private AnimalInteractionEvents() {}
    private static final java.util.List<java.util.function.BiPredicate<ServerPlayer, Mob>> INSPECTIONS = new java.util.ArrayList<>();
    public static void inspection(java.util.function.BiPredicate<ServerPlayer, Mob> contribution) { INSPECTIONS.add(contribution); }

    public static void interact(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Mob mob)) return;
        var stack = event.getItemStack();
        boolean ownItem = stack.getItem() instanceof AnimalCareItem;
        boolean charm = stack.getItem() instanceof io.github.dirtyredz.herbalwitch.companions.animals.capture.CaptureCharmItem;
        boolean salve = stack.is(PreparationItems.product(EverydayProductCatalog.HEALING_SALVE));
        boolean book = GrimoireItems.isGrimoire(stack) && AnimalEligibility.profile(mob).isPresent();
        if (!ownItem && !salve && !book && !charm) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        long tick = player.server.getTickCount();
        if (USED.getOrDefault(player.getUUID(), Long.MIN_VALUE) == tick || !allowRequest(player)) return;
        USED.put(player.getUUID(), tick);
        if (charm) {
            String reason = io.github.dirtyredz.herbalwitch.companions.animals.capture.CaptureService.capture(player, mob, event.getHand());
            io.github.dirtyredz.herbalwitch.companions.animals.capture.CaptureCharmItem.message(player, reason); return;
        }
        AnimalCareAccess.Reason result;
        if (ownItem) result = ((AnimalCareItem) stack.getItem()).apply(player, mob, event.getHand());
        else if (salve) result = AnimalCareService.heal(player, mob, event.getHand());
        else {
            for (var inspection : INSPECTIONS) if (inspection.test(player, mob)) return;
            AnimalMenu.open(player, mob); return;
        }
        if (result != AnimalCareAccess.Reason.READY) message(player, result);
        if (player.containerMenu instanceof AnimalMenu menu) menu.send(player, true);
    }

    public static void message(ServerPlayer player, AnimalCareAccess.Reason reason) {
        player.displayClientMessage(Component.translatable("animal.herbalwitch.reason." + reason.name().toLowerCase(Locale.ROOT)), true);
    }
    public static boolean allowRequest(ServerPlayer player) { return REQUESTS.allow(player.getUUID(), player.server.getTickCount()); }
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        REQUESTS.forget(event.getEntity().getUUID()); USED.remove(event.getEntity().getUUID());
    }
    public static void reset() { for (UUID id : USED.keySet()) REQUESTS.forget(id); USED.clear(); }
}
