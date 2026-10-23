package io.github.dirtyredz.herbalwitch.magic.charm.equipment;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.charm.item.ProtectiveCharmItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

import java.util.Optional;

/**
 * Charms at work. Each loaded tick of a living player wearing an active charm adds to a per-player
 * tick counter kept in the player's saved data (so swapping or re-equipping charms can never reset a
 * part-second); every {@link #TICKS_PER_CHARGE} ticks the active charm spends one second, and a spent
 * charm breaks. When a mob's blow lands (after armour), the active charm softens it once.
 */
public final class CharmEvents {
    public static final int TICKS_PER_CHARGE = 20;
    private static final String COUNTER = HerbalWitch.MOD_ID + ":charm_ticks";

    private CharmEvents() {
    }

    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || !player.isAlive()) {
            return;
        }
        Optional<ItemStack> charm = CharmEquipment.active(player);
        if (charm.isEmpty()) {
            return;
        }
        wear(player, charm.get(), 1);
    }

    /** Counts {@code ticks} of wearing the active charm (the GameTest seam); returns whether it broke. */
    public static boolean wear(ServerPlayer player, ItemStack charm, int ticks) {
        CompoundTag data = player.getPersistentData();
        var result = CharmCharge.wear(charm, data.getInt(COUNTER), ticks);
        if (result.broke()) player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F, 1.2F);
        data.putInt(COUNTER, result.partialTicks());
        return result.broke();
    }

    /** The partial second counted so far (tests). */
    public static int counter(ServerPlayer player) {
        return player.getPersistentData().getInt(COUNTER);
    }

    /** Softens a landed mob blow once (lowest priority: the final amount). */
    public static void onDamage(LivingDamageEvent event) {
        LivingEntity wearer = event.getEntity();
        if (wearer.level().isClientSide || !(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        CharmEquipment.active(wearer).ifPresent(stack -> {
            ProtectiveCharmItem charm = (ProtectiveCharmItem) stack.getItem();
            if (CharmProtection.guards(charm.spec(), attacker)) {
                event.setAmount(CharmProtection.reduce(charm.spec(), event.getAmount()));
            }
        });
    }
}
