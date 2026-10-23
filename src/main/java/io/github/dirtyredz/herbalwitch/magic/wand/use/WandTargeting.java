package io.github.dirtyredz.herbalwitch.magic.wand.use;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.ForgeMod;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
/** Derives the ray from the server's player heading, never from client-supplied target coordinates. */
public final class WandTargeting {
 private WandTargeting(){}
 public static double reach(ServerPlayer actor){return Math.min(WitchRules.server().wand().reach(),actor.getAttributeValue(ForgeMod.BLOCK_REACH.get()));}
 public static boolean loaded(ServerPlayer actor,Vec3 start,Vec3 end){var box=new AABB(start,end);for(int x=net.minecraft.util.Mth.floor(box.minX)>>4;x<=net.minecraft.util.Mth.floor(box.maxX)>>4;x++)for(int z=net.minecraft.util.Mth.floor(box.minZ)>>4;z<=net.minecraft.util.Mth.floor(box.maxZ)>>4;z++)if(!actor.level().hasChunk(x,z))return false;return true;}
 public static BlockHitResult ray(ServerPlayer actor){var from=actor.getEyePosition();var end=from.add(actor.getLookAngle().scale(reach(actor)));return loaded(actor,from,end)?actor.level().clip(new ClipContext(from,end,ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,actor)):null;}
 public static boolean visible(ServerPlayer actor,Vec3 target){var from=actor.getEyePosition();if(from.distanceToSqr(target)>Math.pow(Math.min(reach(actor),actor.getAttributeValue(ForgeMod.ENTITY_REACH.get())),2)||!loaded(actor,from,target))return false;var hit=actor.level().clip(new ClipContext(from,target,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,actor));return hit.getType()==HitResult.Type.MISS||hit.getLocation().distanceToSqr(target)<.02;}
}
