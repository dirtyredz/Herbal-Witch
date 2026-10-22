package io.github.dirtyredz.herbalwitch.magic.ward.menu;

import io.github.dirtyredz.herbalwitch.magic.ward.service.WardFocus;

import io.github.dirtyredz.herbalwitch.magic.ward.service.WardControls;

import io.github.dirtyredz.herbalwitch.magic.ward.service.WardActivation;

import io.github.dirtyredz.herbalwitch.platform.time.TickTime;

import io.github.dirtyredz.herbalwitch.magic.ward.persistence.WardRecord;
import io.github.dirtyredz.herbalwitch.magic.ward.persistence.WardSavedData;
import io.github.dirtyredz.herbalwitch.magic.ward.registry.WardRegistration;
import io.github.dirtyredz.herbalwitch.magic.ward.service.WardService;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.platform.network.RateLimit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * A ward stone's menu ({@code herbalwitch:magic/ward/stone}): no slots. It shows this viewer's private
 * view of the ward in vanilla menu data (charge left, chosen and maximum radius, profile, whether it
 * protects, whose it is) and its buttons arrive as vanilla menu-button clicks (already checked against
 * the open menu): activate, deactivate, release, radius down and up, next profile. The server decides
 * everything; the owner alone changes anything but a first activation.
 */
public class WardMenu extends AbstractContainerMenu {
    public static final String TITLE = "container.herbalwitch.magic.ward.stone";
    public static final int DATA_SECONDS = 0;
    public static final int DATA_RADIUS = 1;
    public static final int DATA_MAX_RADIUS = 2;
    public static final int DATA_PROFILE = 3;
    /** 0 empty, 1 protecting, 2 charged but suspended (overlap, protection rules), 3 records unreadable. */
    public static final int DATA_STATE = 4;
    /** 0 unbound, 1 the viewer's, 2 another witch's. */
    public static final int DATA_OWNER = 5;
    public static final int DATA_COUNT = 6;

    /** Button ids (vanilla menu-button clicks). */
    public enum Button {
        ACTIVATE, DEACTIVATE, RELEASE, RADIUS_DOWN, RADIUS_UP, PROFILE;

        public int id() {
            return ordinal();
        }
    }

    private static final RateLimit LIMIT = new RateLimit(5, 20);
    private final BlockPos pos;
    private final ContainerData data;

    public WardMenu(int id, Inventory inventory, BlockPos pos, ContainerData data) {
        super(WardRegistration.MENU.get(), id);
        this.pos = pos.immutable();
        this.data = data;
        addDataSlots(data);
    }

    /** Client side, from the opening packet. */
    public WardMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, buf.readBlockPos(), new SimpleContainerData(DATA_COUNT));
    }

    /** Server side, with this viewer's live view. */
    public static WardMenu server(int id, Inventory inventory, Player viewer, BlockPos pos) {
        return new WardMenu(id, inventory, pos, new View((ServerLevel) viewer.level(), pos, viewer));
    }

    public BlockPos pos() {
        return pos;
    }

    public int data(int index) {
        return data.get(index);
    }

    @Override
    public boolean stillValid(Player player) {
        return WardAccess.mayUse(player, pos);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer server) || !(server.level() instanceof ServerLevel level) || server.getServer() == null) {
            return false;
        }
        if (id < 0 || id >= Button.values().length) {
            return false;
        }
        if (!LIMIT.allow(server.getUUID(), server.getServer().getTickCount())) {
            return true;
        }
        String outcome = switch (Button.values()[id]) {
            case ACTIVATE -> WardActivation.activate(server, level, pos);
            case DEACTIVATE -> WardControls.deactivate(server, level, pos);
            case RELEASE -> WardControls.release(server, level, pos);
            case RADIUS_DOWN -> WardControls.radius(server, level, pos, -1);
            case RADIUS_UP -> WardControls.radius(server, level, pos, 1);
            case PROFILE -> WardControls.profile(server, level, pos);
        };
        server.displayClientMessage(Component.translatable(WardService.KEY + outcome), true);
        return true;
    }

    /** One viewer's view of the ward, read live (the focus scan at most once a second). */
    private static final class View implements ContainerData {
        private final ServerLevel level;
        private final BlockPos pos;
        private final Player viewer;
        private long scanned = Long.MIN_VALUE;
        private int maxRadius;

        View(ServerLevel level, BlockPos pos, Player viewer) {
            this.level = level;
            this.pos = pos;
            this.viewer = viewer;
        }

        @Override
        public int get(int index) {
            if (!WardSavedData.get(level).writable()) {
                return index == DATA_STATE ? 3 : 0;
            }
            Optional<WardRecord> record = WardService.record(level, pos);
            return switch (index) {
                case DATA_SECONDS -> record.map(r -> Math.min(Short.MAX_VALUE, TickTime.ceilingSeconds(r.charge()))).orElse(0);
                case DATA_RADIUS -> record.map(WardRecord::radius).orElse(WitchRules.server().workings().wardRadius());
                case DATA_MAX_RADIUS -> maxRadius();
                case DATA_PROFILE -> record.map(r -> r.profile().ordinal()).orElse(2);
                case DATA_STATE -> record.filter(WardRecord::charged).map(r -> WardService.indexed(r.id()) ? 1 : 2).orElse(0);
                case DATA_OWNER -> record.flatMap(WardRecord::owner).map(o -> o.equals(viewer.getUUID()) ? 1 : 2).orElse(0);
                default -> 0;
            };
        }

        private int maxRadius() {
            if (level.getGameTime() - scanned >= 20) {
                scanned = level.getGameTime();
                maxRadius = WardFocus.maxRadius(level, pos);
            }
            return maxRadius;
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    }
}
