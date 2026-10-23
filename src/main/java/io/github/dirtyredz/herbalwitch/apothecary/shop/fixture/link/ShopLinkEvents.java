package io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link;

import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopRegisterBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopLink;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStore;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopAccess;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopSession;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopSessionService;
import io.github.dirtyredz.herbalwitch.apothecary.shop.trade.ShopResult;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;

import java.util.Optional;

/**
 * Forge adapters that keep commerce out of general furniture code: the armed owner's linking click, a
 * linked apothecary counter (or a register's own counter) opening public browsing, and the ownership
 * guard that stops non-owners breaking a bound register, display or board by hand. The counter keeps
 * its decor identity and ordinary destruction rules.
 */
public final class ShopLinkEvents {
    private ShopLinkEvents() {
    }

    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos pos = event.getPos();
        if (ShopLinkService.armed(player)) {
            if (event.getHand() == InteractionHand.MAIN_HAND) {
                ShopResult result = ShopLinkService.click(player, level, pos);
                player.displayClientMessage(Component.translatable(result.key()), true);
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (!ShopFixtureBlock.isCounter(state) || event.getHand() != InteractionHand.MAIN_HAND) return;
        Optional<ShopRecord> shop = counterShop(level, pos);
        if (shop.isEmpty()) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (ShopAccess.actor(player) && ShopAccess.near(player, level, pos) && ShopAccess.mayUse(player, level, pos)) {
            ShopSessionService.open(player, ShopSession.View.BROWSE, shop.get().id, ShopSession.Origin.COUNTER, level, pos);
        }
    }

    /** The shop a counter serves: an explicit counter link, or the counter under the bound register. */
    static Optional<ShopRecord> counterShop(ServerLevel level, BlockPos pos) {
        ShopStore store = ShopStore.get(level.getServer());
        if (!store.ready()) return Optional.empty();
        String dimension = ShopBindingService.dimension(level);
        Optional<ShopRecord> linked = store.indexes().fixture(dimension, pos.asLong()).flatMap(store::record)
                .filter(r -> r.link(dimension, pos.asLong()).filter(l -> l.kind() == ShopLink.Kind.COUNTER).isPresent());
        if (linked.isPresent()) return linked;
        BlockPos above = pos.above();
        if (level.getBlockState(above).getBlock() instanceof ShopRegisterBlock && level.getBlockEntity(above) instanceof ShopFixtureBlockEntity register) {
            return ShopBindingService.resolveRegister(level, above, register);
        }
        return Optional.empty();
    }

    /** Hand-breaking a bound fixture is the owner's alone; explosions and support loss still remove it safely. */
    public static void breakBlock(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || !(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getState().getBlock() instanceof ShopFixtureBlock block) || !(level.getBlockEntity(event.getPos()) instanceof ShopFixtureBlockEntity fixture)) return;
        Optional<ShopRecord> shop = block instanceof ShopRegisterBlock
                ? ShopBindingService.resolveRegister(level, event.getPos(), fixture)
                : ShopFixtureHooks.linkedShop(level, event.getPos(), fixture);
        if (shop.isPresent() && !shop.get().owner.equals(player.getUUID())) {
            event.setCanceled(true);
            player.displayClientMessage(Component.translatable("shop.herbalwitch.result.break_not_owner"), true);
        }
    }
}
