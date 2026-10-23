package io.github.dirtyredz.herbalwitch.magic.wand.fixture;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode;
import io.github.dirtyredz.herbalwitch.magic.wand.persistence.WandItemData;
import io.github.dirtyredz.herbalwitch.magic.wand.registry.WandItems;
public final class WandFixtures {
 public static final BlockPos TARGET=new BlockPos(4,1,4),FEET=new BlockPos(4,1,2);
 private WandFixtures(){}
 /** Retained CI plots may contain older structures above the short empty template. */
 public static void openSky(GameTestHelper h,BlockPos relative){var at=h.absolutePos(relative);for(int y=at.getY()+1;y<h.getLevel().getMaxBuildHeight();y++)h.getLevel().setBlock(new BlockPos(at.getX(),y,at.getZ()),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),net.minecraft.world.level.block.Block.UPDATE_CLIENTS);}

 public static ItemStack wand(WandMode mode){var stack=new ItemStack(WandItems.WAND.get());WandItemData.write(stack,mode);return stack;}
 public static void cast(GameTestHelper h,ServerPlayer actor,WandMode mode,InteractionHand hand,BlockPos target){
  var stack=wand(mode);actor.setItemInHand(hand,stack);var at=h.absolutePos(target);var shape=h.getLevel().getBlockState(at).getShape(h.getLevel(),at);var aim=Vec3.atCenterOf(at).add(0,shape.isEmpty()?0:shape.bounds().maxY-.51,0);actor.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,aim);
  actor.gameMode.useItemOn(actor,h.getLevel(),stack,hand,new BlockHitResult(Vec3.atCenterOf(at),Direction.UP,at,false));
 }
}
