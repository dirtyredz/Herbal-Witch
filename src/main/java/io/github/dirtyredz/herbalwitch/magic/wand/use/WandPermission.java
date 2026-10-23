package io.github.dirtyredz.herbalwitch.magic.wand.use;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import io.github.dirtyredz.herbalwitch.platform.permission.*;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.ProtectionAdapters;
import io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode;
/** Action-specific claim vetoes supplement the established block mutation boundary. */
public final class WandPermission {
 @FunctionalInterface public interface Policy{boolean allowed(WandMode mode,ServerPlayer actor,BlockPos pos);}
 private static final java.util.List<Policy> POLICIES=new CopyOnWriteArrayList<>();
 private WandPermission(){}
 public static void register(Policy policy){POLICIES.add(policy);}
 public static boolean allowed(WandMode mode,ServerPlayer actor,BlockPos pos){if(ProtectionAdapters.restricted()||!ActionPermissionService.mayModify(ActionContext.of(actor,actor.level(),pos)))return false;for(var policy:POLICIES)if(!policy.allowed(mode,actor,pos))return false;return true;}
}
