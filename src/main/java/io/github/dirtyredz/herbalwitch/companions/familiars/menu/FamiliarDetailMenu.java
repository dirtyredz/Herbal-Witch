package io.github.dirtyredz.herbalwitch.companions.familiars.menu;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraftforge.network.NetworkHooks;
import java.util.UUID;

/** Nearby detail access is revalidated continuously; remote rows never grant inventory access. */
public class FamiliarDetailMenu extends FamiliarRosterMenu {
    public FamiliarDetailMenu(int id, Inventory inventory, FriendlyByteBuf buffer) { this(id, inventory, buffer.readLong(), buffer.readUUID()); }
    public FamiliarDetailMenu(int id, Inventory inventory, long nonce, UUID bond) { super(FamiliarMenus.DETAIL.get(), id, nonce, bond); }
    @Override public boolean stillValid(Player player) {
        return !(player instanceof ServerPlayer owner) || FamiliarMenuAccess.detail(owner, FamiliarStore.get(owner.server).record(selected).orElse(null));
    }
    public static boolean open(ServerPlayer owner, UUID bond) {
        var record = FamiliarStore.get(owner.server).record(bond).orElse(null);
        if (!FamiliarMenuAccess.detail(owner, record)) return false;
        long nonce = issueNonce();
        NetworkHooks.openScreen(owner, new SimpleMenuProvider((id, inventory, actor) -> new FamiliarDetailMenu(id, inventory, nonce, bond),
                Component.translatable("familiar.herbalwitch.detail")), b -> { b.writeLong(nonce); b.writeUUID(bond); });
        if (owner.containerMenu instanceof FamiliarDetailMenu menu) { menu.send(owner, true, ""); return true; }
        return false;
    }
}
