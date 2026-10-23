package io.github.dirtyredz.herbalwitch.decor.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.MountSpec.Support;
import io.github.dirtyredz.herbalwitch.decor.structure.FurnitureLayouts;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.decor.showcase.DecorShowcaseLayout.Footprint;
import io.github.dirtyredz.herbalwitch.decor.showcase.DecorShowcaseLayout.Slot;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement.Line;
import io.github.dirtyredz.herbalwitch.decor.behavior.storage.FurnitureInventory;
import io.github.dirtyredz.herbalwitch.decor.behavior.storage.FurnitureStorageEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * What the decor section shows for each piece: one canonical labelled sample per piece, facing the
 * entrance (north), with the walls and ceilings it needs, lights lit, storage stocked, plus explicit
 * extra samples for meaningful alternate states (the door's other hinge and open states). Placements
 * carry lazy states, so planning never touches the registries.
 */
public final class DecorShowcaseSamples {
    public static final String LABEL = "showcase." + HerbalWitch.MOD_ID + ".label.decor.";
    /** The alternate door samples after the canonical closed, left-hinged one. */
    public static final List<String> DOOR_STATES = List.of("hinge_right", "open_left", "open_right");
    private static final Supplier<BlockState> WALL = () -> Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState();
    private static final int SIGN_LINE = 15;

    /** One sample: a piece in a state ({@code null} = canonical). */
    public record Sample(FurnitureSpec spec, String state) {
        public Footprint footprint() {
            int[] size = spec.mount().size();
            Support support = spec.support();
            return new Footprint(size[0], size[1], support == Support.WALL || support == Support.CORNER, support == Support.CORNER);
        }
    }

    private DecorShowcaseSamples() {
    }

    /** Every sample in catalogue order: each piece once, the door followed by its alternate states. */
    public static List<Sample> samples() {
        List<Sample> out = new ArrayList<>();
        for (FurnitureSpec spec : FurnitureCatalog.ALL) {
            out.add(new Sample(spec, null));
            if (spec.behavior() == FurnitureBehavior.DOOR) {
                DOOR_STATES.forEach(state -> out.add(new Sample(spec, state)));
            }
        }
        return out;
    }

    /** The sample's placements in its slot. */
    public static List<ShowcasePlacement> place(Sample sample, Slot slot, Consumer<BlockEntity> stock) {
        FurnitureSpec spec = sample.spec();
        Footprint f = sample.footprint();
        int originX = slot.x() + f.width() - 1;
        int originZ = slot.z();
        List<ShowcasePlacement> out = new ArrayList<>();
        out.add(ShowcasePlacement.label(slot.x() - DecorShowcaseLayout.SIGN_COLUMN, slot.z(), label(sample)));
        MultiblockLayout layout = FurnitureLayouts.of(spec);
        int cells = layout == null ? 1 : layout.size();
        for (int i = 0; i < cells; i++) {
            BlockPos at = layout == null ? BlockPos.ZERO : layout.offset(Direction.NORTH, i);
            int x = originX + at.getX();
            int y = 1 + at.getY();
            int z = originZ + at.getZ();
            int index = i;
            boolean origin = i == 0;
            out.add(ShowcasePlacement.content(x, y, z, () -> cellState(sample, index), origin && stores(spec) ? stock : null));
            boolean rear = layout == null || layout.rear(i);
            boolean top = layout == null || layout.top(i);
            switch (spec.support()) {
                case WALL -> {
                    if (rear) {
                        out.add(ShowcasePlacement.support(x, y, z + 1, WALL));
                    }
                }
                case CORNER -> {
                    if (rear) {
                        out.add(ShowcasePlacement.support(x, y, z + 1, WALL));
                        out.add(ShowcasePlacement.support(x + 1, y, z, WALL));
                    }
                }
                case CEILING -> {
                    if (top) {
                        out.add(ShowcasePlacement.support(x, y + 1, z, WALL));
                    }
                }
                default -> {
                }
            }
        }
        return out;
    }

    static boolean stores(FurnitureSpec spec) {
        return spec.behavior() == FurnitureBehavior.STORAGE || spec.behavior() == FurnitureBehavior.DISPLAY;
    }

    /**
     * Stocks one storage sample after placement: a cabinet gets a few ordinary stacks, a display one
     * item on every other slot (so empty and filled anchors both show).
     */
    public static void stock(BlockEntity entity) {
        if (!(entity instanceof FurnitureStorageEntity storage) || !storage.isOrigin()) {
            return;
        }
        FurnitureInventory inventory = storage.inventory();
        List<ItemStack> samples = List.of(new ItemStack(Items.GLASS_BOTTLE), new ItemStack(Items.BOOK), new ItemStack(Items.FLOWER_POT),
                new ItemStack(Items.CANDLE), new ItemStack(Items.HONEY_BOTTLE));
        if (storage.storage().display()) {
            for (int i = 0; i < inventory.getSlots(); i += 2) {
                inventory.setStackInSlot(i, samples.get((i / 2) % samples.size()).copy());
            }
        } else {
            inventory.setStackInSlot(0, new ItemStack(Items.DARK_OAK_PLANKS, 32));
            inventory.setStackInSlot(1, new ItemStack(Items.BREAD, 8));
            inventory.setStackInSlot(4, new ItemStack(Items.GLASS_BOTTLE, 16));
        }
    }

    /** The sign: the piece's name over up to three lines, and the state of an alternate sample. */
    static Line[] label(Sample sample) {
        List<Line> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : sample.spec().displayName().split(" ")) {
            if (line.length() > 0 && line.length() + 1 + word.length() > SIGN_LINE) {
                lines.add(Line.literal(line.toString()));
                line.setLength(0);
            }
            line.append(line.length() > 0 ? " " : "").append(word);
        }
        lines.add(Line.literal(line.toString()));
        if (sample.state() != null) {
            lines.add(Line.translated(LABEL + sample.state()));
        }
        return lines.toArray(Line[]::new);
    }

    /** The block state of one cell of a sample, facing north. */
    static BlockState cellState(Sample sample, int index) {
        Block block = DecorBlocks.block(sample.spec());
        BlockState origin = origin(sample, block);
        return block instanceof StructurePlacement structure ? structure.cellState(origin, index) : origin;
    }

    /**
     * A sample's origin state: facing the entrance (a bed's vanilla facing points from foot to head,
     * so it faces south to put its foot toward the entrance), lights lit, a door in its sample state.
     */
    static BlockState origin(Sample sample, Block block) {
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, block instanceof BedBlock ? Direction.SOUTH : Direction.NORTH);
        }
        if (state.hasProperty(BlockStateProperties.LIT)) {
            state = state.setValue(BlockStateProperties.LIT, true);
        }
        if (block instanceof DoorBlock && sample.state() != null) {
            state = state.setValue(DoorBlock.HINGE, sample.state().endsWith("right") ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT)
                    .setValue(DoorBlock.OPEN, sample.state().startsWith("open"));
        }
        return state;
    }
}
