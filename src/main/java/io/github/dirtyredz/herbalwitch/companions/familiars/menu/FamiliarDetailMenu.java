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
import io.github.dirtyredz.herbalwitch.companions.familiars.equipment.*;
import io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalInteractionEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** Nearby detail access is revalidated continuously; remote rows never grant inventory access. */
public class FamiliarDetailMenu extends FamiliarRosterMenu {
    private final SimpleContainer equipment = new SimpleContainer(2);
    private final FamiliarEquipmentService transfer;
    private final Player actor;
    private boolean clicking;
    private boolean powersView;
    public void powersView(boolean value) { powersView = value; }
    public FamiliarDetailMenu(int id, Inventory inventory, FriendlyByteBuf buffer) { this(id, inventory, buffer.readLong(), buffer.readUUID()); }
    public FamiliarDetailMenu(int id, Inventory inventory, long nonce, UUID bond) {
        super(FamiliarMenus.DETAIL.get(), id, nonce, bond); actor = inventory.player; transfer = new FamiliarEquipmentService(bond);
        for (int i = 0; i < 2; i++) {
            final int equipmentSlot = i;
            addSlot(new Slot(equipment, i, 18 + i * 68, 126) {
                @Override public boolean isActive() { return !powersView; }
                @Override public int getMaxStackSize() { return 1; }
                @Override public boolean mayPlace(ItemStack stack) { return accepts(equipmentSlot, stack); }
                @Override public boolean mayPickup(Player player) { return stillValid(player); }
            });
        }
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlot(new Slot(inventory, column + row * 9 + 9, 59 + column * 18, 148 + row * 18) { @Override public boolean isActive() { return !powersView; } });
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 59 + column * 18, 206) { @Override public boolean isActive() { return !powersView; } });
        refresh();
    }
    private boolean accepts(int slot, ItemStack stack) {
        if (!(actor instanceof ServerPlayer owner)) return stack.isEmpty() || stack.getCount() == 1;
        var record = FamiliarStore.get(owner.server).record(selected).orElse(null);
        return record != null && stillValid(owner) && FamiliarEquipmentRules.accepts(record.species(), slot, stack);
    }
    private void refresh() {
        if (!(actor instanceof ServerPlayer owner) || transfer.active()) return;
        FamiliarStore.get(owner.server).record(selected).ifPresent(r -> {
            equipment.setItem(0, FamiliarEquipmentInventory.item(r, 0)); equipment.setItem(1, FamiliarEquipmentInventory.item(r, 1));
        });
    }
    @Override public boolean stillValid(Player player) {
        if (!(player instanceof ServerPlayer owner)) return true;
        var record = FamiliarStore.get(owner.server).record(selected).orElse(null);
        if (!transfer.active()) return FamiliarMenuAccess.detail(owner, record);
        if (!transfer.owns(owner) || !FamiliarMenuAccess.roster(owner)) return false;
        var mob = FamiliarMenuAccess.loaded(owner, record);
        return mob != null && mob.level() == owner.level() && owner.canReach(mob, 0) && owner.hasLineOfSight(mob);
    }
    @Override public void broadcastChanges() { if (!clicking) refresh(); super.broadcastChanges(); }
    @Override public void clicked(int slot, int button, ClickType type, Player player) {
        if (!(player instanceof ServerPlayer owner)) { super.clicked(slot, button, type, player); return; }
        // Outside drops, drag distribution and creative cloning cannot bypass the two transfer slots.
        if (slot < 0 || slot >= slots.size() || !stillValid(owner)
                || type != ClickType.PICKUP && type != ClickType.QUICK_MOVE && type != ClickType.SWAP) { broadcastChanges(); return; }
        boolean touches = transfer.active() || slot < 2 || type == ClickType.QUICK_MOVE;
        if (!touches) { super.clicked(slot, button, type, owner); return; }
        if (type == ClickType.SWAP && (button < 0 || button > 8 || slot < 2 && !accepts(slot, owner.getInventory().getItem(button)))) return;
        if (slot < 2 && type == ClickType.PICKUP && !getCarried().isEmpty() && !accepts(slot, getCarried())) return;
        if (type == ClickType.QUICK_MOVE) {
            var source = slots.get(slot).getItem();
            if (source.isEmpty()) return;
            if (slot < 2 && FamiliarInventoryDeposit.plan(owner.getInventory(), source).isEmpty()) return;
            if (slot >= 2) {
                int destination = accepts(0, source) ? 0 : accepts(1, source) ? 1 : -1;
                if (destination < 0 || slots.get(destination).hasItem()) return;
            }
        }
        if (type == ClickType.PICKUP && getCarried().isEmpty() && !slots.get(slot).hasItem()) return;
        if (!AnimalInteractionEvents.allowRequest(owner)) return;
        var store = FamiliarStore.get(owner.server); if (!store.begin(owner.server.getTickCount())) return;
        try {
            refresh();
            if (!transfer.begin(owner, getCarried())) return;
            clicking = true;
            super.clicked(slot, button, type, owner);
            transfer.commit(owner, equipment.getItem(0), equipment.getItem(1), getCarried());
        } finally { clicking = false; store.end(); broadcastChanges(); }
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (!clicking || index < 0 || index >= slots.size() || !stillValid(player)) return ItemStack.EMPTY;
        var slot = slots.get(index); if (!slot.hasItem()) return ItemStack.EMPTY;
        var source = slot.getItem(); var before = source.copy();
        if (index < 2) { if (!moveItemStackTo(source, 2, slots.size(), true)) return ItemStack.EMPTY; }
        else {
            int destination = accepts(0, source) ? 0 : accepts(1, source) ? 1 : -1;
            if (destination < 0 || !moveItemStackTo(source, destination, destination + 1, false)) return ItemStack.EMPTY;
        }
        if (source.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, source); return before;
    }
    @Override public void removed(Player player) {
        if (!(player instanceof ServerPlayer owner) || !transfer.active()) { super.removed(player); return; }
        // closeContainer normally scatters a full inventory's cursor. Keep that case as durable escrow instead.
        var carried = getCarried().copy();
        if (!carried.isEmpty() && FamiliarInventoryDeposit.plan(owner.getInventory(), carried).map(plan -> plan.apply(owner.getInventory())).orElse(false)) carried = ItemStack.EMPTY;
        setCarried(carried);
        if (carried.isEmpty()) transfer.commit(owner, equipment.getItem(0), equipment.getItem(1), carried);
        else if (transfer.escrow(owner, carried)) setCarried(ItemStack.EMPTY);
        // Never invoke vanilla's drop path for a transfer that could not be verified.
    }
    public String collectCargo(ServerPlayer owner) {
        if (transfer.active() || !getCarried().isEmpty() || !stillValid(owner)) return "unavailable";
        var store = FamiliarStore.get(owner.server); var r = store.record(selected).orElseThrow(); var cargo = FamiliarEquipmentInventory.cargo(r);
        if (cargo.isEmpty()) return "cargo_empty";
        var deposit = FamiliarInventoryDeposit.plan(owner.getInventory(), cargo).orElse(null);
        if (deposit == null) return "inventory_full";
        if (!store.begin(owner.server.getTickCount())) return "unavailable";
        try {
            if (!transfer.begin(owner, ItemStack.EMPTY)) return "interrupted";
            if (!store.update(FamiliarEquipmentInventory.cargo(store.record(selected).orElseThrow(), ItemStack.EMPTY), true)
                    || !deposit.apply(owner.getInventory())) { transfer.quarantine(owner); return "interrupted"; }
            return transfer.commit(owner, equipment.getItem(0), equipment.getItem(1), ItemStack.EMPTY) ? "cargo_collected" : "interrupted";
        } finally { store.end(); broadcastChanges(); }
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
