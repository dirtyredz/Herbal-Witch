package io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * A stock shelf or jar: the fixture identity plus a public projection of up to three offers (a
 * sample stack of one, availability, lot and price). The projection is a picture, never inventory:
 * nothing can be extracted from it, it carries no capability and it is rebuilt from the canonical
 * shop record. A showcase sample is marked as a demonstration and never accepts a link.
 */
public class ShopDisplayBlockEntity extends ShopFixtureBlockEntity {
    /** One displayed offer. */
    public record Sample(ItemStack stack, boolean available, int lot, int price) {
        public Sample {
            stack = stack.copyWithCount(1);
        }
    }

    private List<Sample> samples = List.of();
    private boolean demonstration;

    public ShopDisplayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public List<Sample> samples() {
        return samples;
    }

    public boolean demonstration() {
        return demonstration;
    }

    /** Showcase only: a fixed, visibly marked projection that belongs to no shop. */
    public void demonstrate(List<Sample> fixed) {
        demonstration = true;
        project(fixed);
    }

    /** Replaces the projection; sends a block update only when it changed. */
    public void project(List<Sample> next) {
        List<Sample> bounded = List.copyOf(next.subList(0, Math.min(next.size(), slots())));
        if (same(bounded)) return;
        samples = bounded;
        setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    private boolean same(List<Sample> other) {
        if (other.size() != samples.size()) return false;
        for (int i = 0; i < other.size(); i++) {
            Sample a = other.get(i), b = samples.get(i);
            if (a.available() != b.available() || a.lot() != b.lot() || a.price() != b.price() || !ItemStack.matches(a.stack(), b.stack())) return false;
        }
        return true;
    }

    public int slots() {
        return getBlockState().getBlock() instanceof ShopFixtureBlock block ? block.spec().displaySlots() : 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Display", projection());
    }

    private CompoundTag projection() {
        CompoundTag display = new CompoundTag();
        ListTag list = new ListTag();
        for (Sample sample : samples) {
            CompoundTag entry = sample.stack().save(new CompoundTag());
            entry.putBoolean("available", sample.available());
            entry.putInt("lot", sample.lot());
            entry.putInt("price", sample.price());
            list.add(entry);
        }
        display.put("samples", list);
        if (demonstration) display.putBoolean("demonstration", true);
        return display;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        CompoundTag display = tag.getCompound("Display");
        demonstration = display.getBoolean("demonstration");
        List<Sample> out = new ArrayList<>();
        ListTag list = display.getList("samples", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(3, list.size()); i++) {
            CompoundTag entry = list.getCompound(i);
            ItemStack stack = ItemStack.of(entry);
            if (!stack.isEmpty()) out.add(new Sample(stack, entry.getBoolean("available"), entry.getInt("lot"), entry.getInt("price")));
        }
        samples = List.copyOf(out);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("Display", projection());
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
