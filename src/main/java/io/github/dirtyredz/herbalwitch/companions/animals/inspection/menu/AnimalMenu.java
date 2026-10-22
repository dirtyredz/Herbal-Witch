package io.github.dirtyredz.herbalwitch.companions.animals.inspection.menu;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationEffects;
import io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalInteractionEvents;
import io.github.dirtyredz.herbalwitch.companions.animals.care.taming.AnimalOwnership;
import io.github.dirtyredz.herbalwitch.companions.animals.care.taming.AnimalTamingService;
import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalServerConfig;
import io.github.dirtyredz.herbalwitch.companions.animals.inspection.network.AnimalPackets;
import io.github.dirtyredz.herbalwitch.companions.animals.inspection.network.AnimalRequest;
import io.github.dirtyredz.herbalwitch.companions.animals.inspection.network.AnimalSnapshot;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalMenus;
import io.github.dirtyredz.herbalwitch.companions.animals.species.AnimalEligibility;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalCapability;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalState;
import io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalTrustService;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.platform.network.RateLimit;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionContext.Action;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.network.NetworkHooks;
import java.security.SecureRandom;
import java.util.UUID;

/** A zero-slot, continuously validated animal session. Target lookups never load chunks. */
public final class AnimalMenu extends AbstractContainerMenu {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final UUID target;
    private final long nonce;
    private final long generation;
    private long revision;
    private long sentAt = Long.MIN_VALUE / 2;

    public AnimalMenu(int id, Inventory inventory, UUID target, long nonce) {
        super(AnimalMenus.CARD.get(), id);
        this.target = target; this.nonce = nonce; this.generation = AnimalServerConfig.generation();
    }
    public AnimalMenu(int id, Inventory inventory, FriendlyByteBuf buf) { this(id, inventory, buf.readUUID(), buf.readLong()); }
    public UUID target() { return target; }
    public long nonce() { return nonce; }
    public long revision() { return revision; }
    public Mob animal(ServerPlayer player) { return player.serverLevel().getEntity(target) instanceof Mob mob ? mob : null; }

    private static boolean access(ServerPlayer player, Mob mob) {
        return mob != null && mob.isAlive() && !mob.isRemoved() && player.isAlive() && !player.isSpectator()
                && player.level() == mob.level() && player.canReach(mob, 0) && player.hasLineOfSight(mob)
                && player.getInventory().hasAnyMatching(GrimoireItems::isGrimoire)
                && AnimalEligibility.profile(mob).isPresent() && EntityActionPermission.check(Action.ANIMAL_CARE, player, mob).allowed();
    }
    @Override public boolean stillValid(Player player) {
        return !(player instanceof ServerPlayer server) || generation == AnimalServerConfig.generation() && access(server, animal(server));
    }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    public static boolean open(ServerPlayer player, Mob mob) {
        if (!access(player, mob)) return false;
        long nonce; do { nonce = RANDOM.nextLong(); } while (nonce == 0);
        long issued = nonce;
        NetworkHooks.openScreen(player, new SimpleMenuProvider((id, inventory, actor) -> new AnimalMenu(id, inventory, mob.getUUID(), issued),
                Component.translatable("animal.herbalwitch.title")), buf -> { buf.writeUUID(mob.getUUID()); buf.writeLong(issued); });
        if (player.containerMenu instanceof AnimalMenu menu) { menu.send(player, true); return true; }
        return false;
    }

    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player && player.containerMenu instanceof AnimalMenu menu) {
            if (!menu.stillValid(player)) player.closeContainer(); else menu.send(player, false);
        }
    }

    public void send(ServerPlayer player, boolean immediate) {
        long tick = player.server.getTickCount();
        if (!stillValid(player) || !immediate && tick - sentAt < 10) return;
        Mob mob = animal(player);
        var profile = AnimalEligibility.profile(mob).orElseThrow();
        var relation = AnimalTrustService.relationship(mob, player.getUUID());
        var state = AnimalCapability.of(mob).flatMap(AnimalCapability::state);
        var ownership = AnimalOwnership.of(mob, profile);
        int owner = ownership.conflict() ? 3 : ownership.owner() == null ? 0 : ownership.ownedBy(player.getUUID()) ? 1 : 2;
        var rules = AnimalServerConfig.current();
        int actions = (profile.adoptable() && rules.relationships() && rules.adoption() ? 1 : 0) | (profile.nativeTaming() ? 2 : 0)
                | (profile.goalAi() && rules.calming() && mob.getType().is(io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalTags.CALMABLE) ? 4 : 0)
                | (profile.capturable() && rules.captures() && mob.getType().is(io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalTags.CAPTURABLE) ? 8 : 0);
        var sleep = mob.getEffect(PreparationEffects.SLEEP.get());
        AnimalPackets.send(player, new AnimalSnapshot(containerId, nonce, target, ++revision, EntityType.getKey(mob.getType()).toString(),
                relation.trust(), relation.feeds(), (int) relation.remaining(AnimalTrustService.clock(mob)),
                state.map(s -> (int) s.hurtRemaining()).orElse(0), state.map(s -> (int) s.healRemaining()).orElse(0),
                state.map(s -> (int) s.calmRemaining()).orElse(0), sleep == null ? 0 : Math.max(0, sleep.getDuration()),
                owner, actions, AnimalTamingService.requirements(player, mob)));
        sentAt = tick;
    }

    public static void request(ServerPlayer player, AnimalRequest request) {
        if (!(player.containerMenu instanceof AnimalMenu menu)
                || !AnimalInteractionEvents.allowRequest(player)
                || !request.matches(menu.containerId, menu.nonce, menu.target, menu.revision) || !menu.stillValid(player)) return;
        var result = AnimalTamingService.adopt(player, menu.animal(player));
        AnimalInteractionEvents.message(player, result);
        menu.send(player, true);
    }
}
