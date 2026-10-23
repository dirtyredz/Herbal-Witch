package io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link;

import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopDisplayBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopDisplayBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopOrderBoardBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopRegisterBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStore;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopAccess;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopSession;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopSessionService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * The physical adapters' only entry into commerce. A register opens management for its owner, browsing
 * for everyone else, or the create/reconnect view while unbound; a linked display opens its selected
 * offers; a linked board opens requests. Removal detaches a register or unlinks a fixture.
 */
public final class ShopFixtureHooks {
    private ShopFixtureHooks() {
    }

    public static InteractionResult use(ShopFixtureBlock block, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer server) || !(level instanceof ServerLevel serverLevel)
                || !(level.getBlockEntity(pos) instanceof ShopFixtureBlockEntity fixture)) return InteractionResult.PASS;
        if (ShopLinkService.armed(server)) return InteractionResult.PASS; // the link event consumes this click
        if (!ShopAccess.actor(server) || !ShopAccess.near(server, level, pos) || !ShopAccess.mayUse(server, level, pos)) {
            return InteractionResult.FAIL;
        }
        if (!ShopStore.get(server.server).ready()) {
            server.displayClientMessage(Component.translatable("shop.herbalwitch.result.indexing"), true);
            return InteractionResult.CONSUME;
        }
        if (block instanceof ShopRegisterBlock) {
            openRegister(server, serverLevel, pos, fixture);
            return InteractionResult.CONSUME;
        }
        Optional<ShopRecord> record = linkedShop(serverLevel, pos, fixture);
        if (record.isEmpty()) {
            server.displayClientMessage(Component.translatable("shop.herbalwitch.result.fixture_unlinked"), true);
            return InteractionResult.CONSUME;
        }
        if (block instanceof ShopDisplayBlock) {
            ShopSessionService.open(server, ShopSession.View.BROWSE, record.get().id, ShopSession.Origin.DISPLAY, serverLevel, pos);
        } else if (block instanceof ShopOrderBoardBlock) {
            ShopSessionService.open(server, ShopSession.View.ORDERS, record.get().id, ShopSession.Origin.BOARD, serverLevel, pos);
        }
        return InteractionResult.CONSUME;
    }

    public static void openRegister(ServerPlayer player, ServerLevel level, BlockPos pos, ShopFixtureBlockEntity register) {
        Optional<ShopRecord> record = ShopBindingService.resolveRegister(level, pos, register);
        if (record.isEmpty()) {
            ShopSessionService.open(player, ShopSession.View.CREATE, null, ShopSession.Origin.REGISTER, level, pos);
            return;
        }
        ShopSession.View view = record.get().owner.equals(player.getUUID()) ? ShopSession.View.MANAGE : ShopSession.View.BROWSE;
        ShopSessionService.open(player, view, record.get().id, ShopSession.Origin.REGISTER, level, pos);
    }

    /** The shop a display or board belongs to, only when the record links this exact instance here. */
    public static Optional<ShopRecord> linkedShop(ServerLevel level, BlockPos pos, ShopFixtureBlockEntity fixture) {
        if (fixture.shop() == null || fixture instanceof ShopDisplayBlockEntity display && display.demonstration()) return Optional.empty();
        return ShopStore.get(level.getServer()).record(fixture.shop()).filter(r -> !r.retired() && ShopBindingService.linked(r, level, pos, fixture));
    }

    public static void removed(ShopFixtureBlock block, Level level, BlockPos pos, ShopFixtureBlockEntity fixture) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (block instanceof ShopRegisterBlock) ShopBindingService.registerRemoved(serverLevel, pos, fixture);
        else ShopLinkService.fixtureRemoved(serverLevel, pos, fixture);
    }
}
