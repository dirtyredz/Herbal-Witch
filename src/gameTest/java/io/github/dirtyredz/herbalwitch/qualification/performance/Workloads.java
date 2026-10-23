package io.github.dirtyredz.herbalwitch.qualification.performance;

import com.mojang.authlib.GameProfile;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.garden.HerbBedBlock;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.apothecary.stations.block.ProcessStationEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.block.StationBlock;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlocks;
import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarRecord;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarBinding;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarHomeKind;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarCommand;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarCommands;
import io.github.dirtyredz.herbalwitch.companions.familiars.home.FamiliarHomeBlockEntity;
import io.github.dirtyredz.herbalwitch.companions.familiars.home.FamiliarHomeService;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarEntities;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarHomes;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.MountSpec;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.magic.broom.registry.BroomEntities;
import io.github.dirtyredz.herbalwitch.magic.broom.registry.BroomItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * The frozen qualification workloads (docs/qualification/performance.md), each built on its own
 * force-loaded plot far from the GameTest plots. Population sizes are fixed here, before any
 * measurement, and reported with the results.
 */
final class Workloads {
    static final int IDLE_BLOCK_ENTITIES = 256;
    static final int PROCESSORS = 128;
    static final int OWNERS = 8;
    static final int MOBILE_PER_OWNER = 3;
    static final int HOME_GUARDS = 32;
    static final int BROOMS = 8;
    static final int VILLAGERS = 16;
    static final int ANIMALS = 200;
    private static final int SIZE = 48;

    private final ServerLevel level;
    private final List<ChunkPos> forced = new ArrayList<>();
    final List<ServerPlayer> owners = new ArrayList<>();

    Workloads(ServerLevel level) {
        this.level = level;
    }

    /** A cleared, floored, force-loaded 48 x 48 plot whose north-west floor corner is {@code base}. */
    void plot(BlockPos base) {
        for (int cx = base.getX() >> 4; cx <= (base.getX() + SIZE) >> 4; cx++) {
            for (int cz = base.getZ() >> 4; cz <= (base.getZ() + SIZE) >> 4; cz++) {
                level.setChunkForced(cx, cz, true);
                forced.add(new ChunkPos(cx, cz));
            }
        }
        for (int x = 0; x < SIZE; x++) for (int z = 0; z < SIZE; z++) {
            level.setBlock(base.offset(x, -1, z), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
            for (int y = 0; y < 6; y++) level.setBlock(base.offset(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    void release() {
        forced.forEach(c -> level.setChunkForced(c.x, c.z, false));
        forced.clear();
    }

    /** Idle cottage: 256 Herbal Witch block entities and a 16 x 16 herb garden, nothing working. */
    int idleCottage(BlockPos base) {
        plot(base);
        int placed = 0;
        Block[] stations = {StationBlocks.MORTAR.get(), StationBlocks.KETTLE.get(), StationBlocks.INFUSION_POT.get()};
        for (int i = 0; i < 96; i++) placed += station(stations[i % 3], cell(base, i)) ? 1 : 0;
        placed += furniture(base, 96, 96, true);
        for (int i = 0; i < 32; i++) placed += place(cell(base, 192 + i), PreparationBlocks.OIL_LANTERN.get().defaultBlockState()) ? 1 : 0;
        for (int i = 0; i < 32; i++) placed += station(StationBlocks.HEARTH.get(), base.offset(2 + (i % 8) * 3, 0, 30 + (i / 8) * 3)) ? 1 : 0;
        garden(base.offset(30, 0, 30));
        return placed;
    }

    /** Busy settlement: 128 processors (64 grinding), 128 more block entities, familiars, brooms, villagers, animals. */
    int busySettlement(BlockPos base, String tag) {
        plot(base);
        int placed = 0;
        for (int i = 0; i < PROCESSORS; i++) {
            BlockPos at = cell(base, i);
            boolean active = i % 2 == 0;
            placed += station(active ? StationBlocks.MORTAR.get() : StationBlocks.KETTLE.get(), at) ? 1 : 0;
            if (active && level.getBlockEntity(at) instanceof ProcessStationEntity mortar) {
                mortar.inventory().setStackInSlot(0, new ItemStack(PreparationItems.bundle(HerbCatalog.SAGE), 64));
            }
        }
        placed += furniture(base, PROCESSORS, 128, true);
        familiars(base.offset(4, 0, 34), tag);
        for (int i = 0; i < BROOMS; i++) {
            var broom = BroomEntities.BROOM.get().create(level);
            broom.moveTo(base.getX() + 40.5, base.getY(), base.getZ() + 4.5 + i * 2, 0, 0);
            broom.initialize(owners.get(owners.size() - OWNERS + i % OWNERS).getUUID(), new ItemStack(BroomItems.BROOM.get()));
            check(level.addFreshEntity(broom), "workload broom");
        }
        for (int i = 0; i < VILLAGERS; i++) spawn(EntityType.VILLAGER, base.offset(44, 0, 4 + i * 2));
        EntityType<?>[] animals = {EntityType.COW, EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN};
        for (int i = 0; i < ANIMALS; i++) {
            Mob animal = spawn(animals[i % 4], base.offset(20 + (i % 20), 0, 34 + (i / 20)));
            AnimalFixtures.relationship(animal, owners.get(owners.size() - OWNERS + i % OWNERS).getUUID(), 40, 2);
        }
        return placed;
    }

    /** Eight online owners at the three-follower cap and thirty-two loaded offline home guards. */
    private void familiars(BlockPos base, String tag) {
        var store = FamiliarStore.get(level.getServer());
        for (int o = 0; o < OWNERS; o++) {
            ServerPlayer owner = player("perf_" + tag + "_owner_" + o, base.offset(o * 3, 0, 0));
            owners.add(owner);
            for (int f = 0; f < MOBILE_PER_OWNER; f++) {
                FamiliarRecord record = record(owner, base.offset(o * 3, 0, 1 + f));
                check(store.create(record), "workload familiar record");
                wolf(record, base.offset(o * 3, 0, 1 + f));
                check(FamiliarCommands.set(owner, record.bond(), FamiliarCommand.FOLLOW).equals("command_set"), "workload follow");
            }
        }
        for (int g = 0; g < HOME_GUARDS; g++) {
            ServerPlayer keeper = player("perf_" + tag + "_keeper_" + g, base.offset(g % 16, 0, 6 + (g / 16) * 4));
            BlockPos home = base.offset(g % 16, 0, 7 + (g / 16) * 4);
            FamiliarRecord record = record(keeper, home.east());
            check(store.create(record), "workload guard record");
            wolf(record, home.south());
            keeper.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(FamiliarHomes.get(FamiliarHomeKind.BED).item().get()));
            var hit = new BlockHitResult(Vec3.atCenterOf(home.below()).relative(Direction.UP, .5), Direction.UP, home.below(), false);
            check(keeper.getMainHandItem().useOn(new UseOnContext(keeper, InteractionHand.MAIN_HAND, hit)).consumesAction(), "workload home");
            keeper.getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get()));
            check(FamiliarHomeService.assign(keeper, (FamiliarHomeBlockEntity) level.getBlockEntity(home), record.bond()).equals("home_assigned"), "workload home assignment");
            check(FamiliarCommands.set(keeper, record.bond(), FamiliarCommand.PROTECT_HOME).equals("command_set"), "workload guard command");
        }
    }

    ServerPlayer player(String name, BlockPos at) {
        ServerPlayer player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.nameUUIDFromBytes(name.getBytes()), name.length() > 16 ? name.substring(name.length() - 16) : name));
        // A real player object (not a FakePlayer) borrows only a fake player's no-op connection.
        player.connection = net.minecraftforge.common.util.FakePlayerFactory.get(level, new GameProfile(UUID.nameUUIDFromBytes((name + "_wire").getBytes()), "wire")).connection;
        player.moveTo(at.getX() + .5, at.getY(), at.getZ() + .5, 0, 0);
        player.getInventory().setItem(0, new ItemStack(GrimoireItems.BOOK.get()));
        return player;
    }

    FamiliarRecord record(ServerPlayer owner, BlockPos at) {
        CompoundTag state = new CompoundTag();
        state.putString("command", "stay");
        state.putString("dimension", level.dimension().location().toString());
        state.putLong("position", at.asLong());
        state.putInt("reserve", 0);
        return new FamiliarRecord(UUID.randomUUID(), owner.getUUID(), UUID.randomUUID(), "wolf", "minecraft:wolf", 1,
                FamiliarRecord.Phase.ACTIVE, UUID.randomUUID(), 1, state, new CompoundTag());
    }

    private void wolf(FamiliarRecord record, BlockPos at) {
        var mob = FamiliarEntities.WOLF.get().create(level);
        mob.setUUID(record.creature());
        mob.setTame(true);
        mob.setOwnerUUID(record.owner());
        FamiliarBinding.of(mob).orElseThrow().bind(record.bond(), record.owner(), record.generation());
        mob.moveTo(at.getX() + .5, at.getY(), at.getZ() + .5, 0, 0);
        check(level.addFreshEntity(mob), "workload familiar admission");
    }

    private Mob spawn(EntityType<?> type, BlockPos at) {
        Mob mob = (Mob) type.create(level);
        mob.moveTo(at.getX() + .5, at.getY(), at.getZ() + .5, 0, 0);
        mob.setPersistenceRequired();
        check(level.addFreshEntity(mob), "workload " + type);
        return mob;
    }

    /** Single-cell standing storage and display pieces, stocked half full. */
    private int furniture(BlockPos base, int first, int count, boolean stocked) {
        List<FurnitureSpec> pieces = new ArrayList<>();
        for (FurnitureSpec spec : FurnitureCatalog.ALL) {
            boolean holds = spec.behavior() == FurnitureBehavior.STORAGE || spec.behavior() == FurnitureBehavior.DISPLAY;
            var support = spec.mount().support();
            if (holds && spec.mount().cells().size() == 1 && (support == MountSpec.Support.FLOOR || support == MountSpec.Support.FREE)) pieces.add(spec);
        }
        check(!pieces.isEmpty(), "no single-cell storage pieces");
        int placed = 0;
        for (int i = 0; i < count; i++) {
            BlockPos at = cell(base, first + i);
            if (!place(at, DecorBlocks.block(pieces.get(i % pieces.size())).defaultBlockState())) continue;
            placed++;
            var entity = level.getBlockEntity(at);
            if (stocked && entity != null) entity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(items -> {
                for (int slot = 0; slot < items.getSlots() / 2; slot++) items.insertItem(slot, new ItemStack(Items.WHEAT, 1), false);
            });
        }
        return placed;
    }

    /** A 16 x 16 joined, moist herb bed with growing herbs above (random ticks stay on). */
    private void garden(BlockPos base) {
        var herbs = HerbCatalog.ALL;
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            BlockPos soil = base.offset(x, 0, z);
            level.setBlock(soil, BotanyBlocks.HERB_BED.get().defaultBlockState().setValue(HerbBedBlock.MOISTURE, 7), Block.UPDATE_ALL);
            var plant = BotanyBlocks.plant(herbs.get((x + z) % herbs.size()));
            level.setBlock(soil.above(), plant.getStateForAge((x * 7 + z) % 4), Block.UPDATE_ALL);
        }
    }

    private boolean station(Block block, BlockPos at) {
        BlockState state = block.defaultBlockState().setValue(StationBlock.FACING, Direction.NORTH);
        if (block instanceof StationBlock station && station.layout() != null) station.placeStation(level, at, state, Block.UPDATE_ALL);
        else level.setBlock(at, state, Block.UPDATE_ALL);
        return level.getBlockEntity(at) != null;
    }

    private boolean place(BlockPos at, BlockState state) {
        level.setBlock(at, state, Block.UPDATE_ALL);
        return level.getBlockEntity(at) != null;
    }

    /** The i-th slot of a 16-wide grid with one free cell between neighbours, from the plot corner. */
    private static BlockPos cell(BlockPos base, int i) {
        return base.offset((i % 16) * 2 + 1, 0, (i / 16) * 2 + 1);
    }
}
