package io.github.dirtyredz.herbalwitch.companions.familiars.menu;

import io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalInteractionEvents;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.network.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.network.NetworkHooks;
import java.security.SecureRandom;
import java.util.*;

/** Private keyset pages and one validated owner session. Reading does not mutate bond state. */
public class FamiliarRosterMenu extends AbstractContainerMenu {
    private static final SecureRandom RANDOM = new SecureRandom();
    protected final long nonce;
    protected final UUID selected;
    private BlockPos homePosition;
    private UUID homeInstance;
    private net.minecraft.world.level.Level homeLevel;
    private UUID cursor;
    private long sentAt = Long.MIN_VALUE / 2, sentRevision;
    public FamiliarRosterMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(FamiliarMenus.ROSTER.get(), id, buffer.readLong(), null);
        if (buffer.readBoolean()) { homePosition = buffer.readBlockPos(); homeInstance = buffer.readUUID(); }
    }
    protected FamiliarRosterMenu(MenuType<?> type, int id, long nonce, UUID selected) { super(type, id); this.nonce = nonce; this.selected = selected; }
    public long nonce() { return nonce; }
    public UUID selected() { return selected; }
    public long sentRevision() { return sentRevision; }
    public boolean homeContext() { return homePosition != null; }
    public io.github.dirtyredz.herbalwitch.companions.familiars.home.FamiliarHomeBlockEntity home(ServerPlayer owner) {
        if (!homeContext() || owner.level() != homeLevel || !owner.level().hasChunkAt(homePosition)) return null;
        if (!(owner.level().getBlockEntity(homePosition) instanceof io.github.dirtyredz.herbalwitch.companions.familiars.home.FamiliarHomeBlockEntity home)
                || !home.instance().equals(homeInstance) || !io.github.dirtyredz.herbalwitch.companions.familiars.home.FamiliarHomeService.mayUse(owner, home, false)) return null;
        return home;
    }
    @Override public boolean stillValid(Player player) { return !(player instanceof ServerPlayer owner) || FamiliarMenuAccess.roster(owner) && (!homeContext() || home(owner) != null); }
    @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
    public static void open(ServerPlayer player) {
        if (!AnimalInteractionEvents.allowRequest(player) || !FamiliarMenuAccess.roster(player)) return;
        long nonce = issueNonce();
        NetworkHooks.openScreen(player, new SimpleMenuProvider((id, inventory, actor) -> new FamiliarRosterMenu(FamiliarMenus.ROSTER.get(), id, nonce, null),
                Component.translatable("familiar.herbalwitch.roster")), b -> { b.writeLong(nonce); b.writeBoolean(false); });
        if (player.containerMenu instanceof FamiliarRosterMenu menu) menu.send(player, true, "");
    }
    public static void openHome(ServerPlayer owner, io.github.dirtyredz.herbalwitch.companions.familiars.home.FamiliarHomeBlockEntity home) {
        if (!AnimalInteractionEvents.allowRequest(owner) || !io.github.dirtyredz.herbalwitch.companions.familiars.home.FamiliarHomeService.mayUse(owner, home, false)) return;
        long nonce = issueNonce();
        NetworkHooks.openScreen(owner, new SimpleMenuProvider((id, inventory, player) -> {
            var menu = new FamiliarRosterMenu(FamiliarMenus.ROSTER.get(), id, nonce, null);
            menu.homePosition = home.getBlockPos(); menu.homeInstance = home.instance(); menu.homeLevel = owner.level(); return menu;
        }, Component.translatable("familiar.herbalwitch.choose_home")), b -> { b.writeLong(nonce); b.writeBoolean(true); b.writeBlockPos(home.getBlockPos()); b.writeUUID(home.instance()); });
        if (owner.containerMenu instanceof FamiliarRosterMenu menu) menu.send(owner, true, "");
    }
    protected static long issueNonce() { long value; do { value = RANDOM.nextLong(); } while (value == 0); return value; }
    public FamiliarRosterPage page(ServerPlayer player, String notice) {
        var store = FamiliarStore.get(player.server); long revision = store.roster().revision(player.getUUID());
        var ids = selected == null ? store.roster().page(player.getUUID(), cursor, 32) : List.of(selected);
        List<FamiliarRosterPage.Row> rows = new ArrayList<>();
        for (UUID id : ids) store.record(id).filter(r -> r.owner().equals(player.getUUID())).ifPresent(r -> rows.add(project(player, r)));
        boolean more = selected == null && !ids.isEmpty() && !store.roster().page(player.getUUID(), ids.get(ids.size() - 1), 1).isEmpty();
        // The actual encoded byte bound, rather than a guessed per-row estimate, governs a page.
        while (!rows.isEmpty()) {
            var candidate = new FamiliarRosterPage(containerId, nonce, revision, rows, more, limit(notice, 128));
            var buffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try { candidate.encode(buffer); return candidate; }
            catch (IllegalArgumentException tooLarge) { rows.remove(rows.size() - 1); more = true; }
            finally { buffer.release(); }
        }
        return new FamiliarRosterPage(containerId, nonce, revision, rows, more, limit(notice, 128));
    }
    private static FamiliarRosterPage.Row project(ServerPlayer player, FamiliarRecord r) {
        var state = r.state(); var live = FamiliarMenuAccess.loaded(player, r); int xp = state.getInt("xp");
        int level = io.github.dirtyredz.herbalwitch.companions.familiars.bond.FamiliarBondProgress.level(xp);
        String dimension = live == null ? state.getString("dimension") : live.level().dimension().location().toString();
        long position = live == null ? state.getLong("position") : live.blockPosition().asLong();
        String home = state.contains("home") ? state.getCompound("home").getString("dimension") + " " + BlockPos.of(state.getCompound("home").getLong("position")).toShortString() : "";
        String name = live == null ? state.getString("name") : live.getName().getString();
        return new FamiliarRosterPage.Row(r.bond(), limit(name, 64), r.species(), level, live == null ? state.getFloat("health") : live.getHealth(),
                live == null ? state.getFloat("maxHealth") : live.getMaxHealth(), r.command().key(), r.phase().name(), live != null,
                limit(dimension, 96), position, limit(home, 128), limit(state.getString("status"), 96), limit(String.join(", ", state.getCompound("buffs").getAllKeys()), 128));
    }
    private static String limit(String value, int length) { return value.length() <= length ? value : value.substring(0, length); }
    public void send(ServerPlayer player, boolean immediate, String notice) {
        long tick = player.server.getTickCount();
        if (!stillValid(player) || !immediate && tick - sentAt < 10) return;
        var page = page(player, notice); sentRevision = page.revision(); sentAt = tick; FamiliarPackets.send(player, page);
    }
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer owner && owner.containerMenu instanceof FamiliarRosterMenu menu) {
            if (!menu.stillValid(owner)) owner.closeContainer(); else menu.send(owner, false, "");
        }
    }
    public static void request(ServerPlayer owner, FamiliarRequest request) {
        if (!(owner.containerMenu instanceof FamiliarRosterMenu menu) || request.container() != menu.containerId || request.nonce() != menu.nonce
                || !menu.stillValid(owner) || !AnimalInteractionEvents.allowRequest(owner)) return;
        var store = FamiliarStore.get(owner.server);
        if (request.revision() != store.roster().revision(owner.getUUID()) || request.revision() != menu.sentRevision) { menu.send(owner, true, "stale"); return; }
        if (request.action().equals("page") && menu.selected == null) {
            if (request.cursor() != null && store.record(request.cursor()).filter(r -> r.owner().equals(owner.getUUID())).isEmpty()) return;
            menu.cursor = request.cursor(); menu.send(owner, true, "");
        } else if (request.action().equals("detail") && request.bond() != null) {
            if (!FamiliarDetailMenu.open(owner, request.bond())) menu.send(owner, true, "nearby_only");
        } else {
            String notice = FamiliarMenuActions.apply(owner, menu, request);
            if (notice != null) menu.send(owner, true, notice);
        }
    }
}
