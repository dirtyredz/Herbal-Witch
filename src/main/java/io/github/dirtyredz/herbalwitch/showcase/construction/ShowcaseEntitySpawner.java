package io.github.dirtyredz.herbalwitch.showcase.construction;

import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlan;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.ArrayList;
import java.util.List;

/** Runs after habitats exist. A failed insertion cleans up only this invocation's exact sample objects. */
public final class ShowcaseEntitySpawner {
    public record Result(int spawned, boolean complete) {}
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ShowcaseEntitySpawner.class);
    private ShowcaseEntitySpawner() {}

    /** Why this sample cannot stand where it is, or null after it has been added to the level. */
    private static String refusal(ShowcasePlan plan, ServerLevel level, ServerPlayer actor, BlockPos origin, Mob mob) {
        var box = mob.getBoundingBox();
        if (box.minX < origin.getX() || box.maxX > origin.getX() + plan.width() || box.minZ < origin.getZ()
                || box.maxZ > origin.getZ() + plan.depth() || box.minY < origin.getY() + 1 || box.maxY > origin.getY() + ShowcasePlan.HEIGHT) return "outside the planned box " + box;
        if (!level.hasChunkAt(mob.blockPosition())) return "chunk not loaded";
        if (!level.noCollision(mob, box)) return "collides with a block";
        if (!EntityActionPermission.checkHarm(actor, mob).allowed()) return "permission denied";
        if (!level.addFreshEntity(mob)) return "the level refused the entity";
        if (level.getEntity(mob.getUUID()) != mob || mob.isRemoved()) return "not present after adding";
        return null;
    }

    public static Result spawn(ShowcasePlan plan, ServerLevel level, ServerPlayer actor, BlockPos origin) {
        List<Mob> created = new ArrayList<>(); boolean complete = false;
        try {
            for (var sample : plan.entities()) {
                var type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(sample.type()));
                if (type == null || !(type.create(level) instanceof Mob mob)) return new Result(0, false);
                created.add(mob);
                mob.moveTo(origin.getX() + sample.x(), origin.getY() + sample.y(), origin.getZ() + sample.z(), 0, 0);
                if (mob instanceof AgeableMob ageable) ageable.setAge(0);
                mob.setPersistenceRequired();
                String refused = refusal(plan, level, actor, origin, mob);
                if (refused != null) {
                    LOGGER.warn("Showcase sample {} at {} refused: {}", sample.type(), mob.blockPosition(), refused);
                    return new Result(0, false);
                }
                if (sample.inert()) io.github.dirtyredz.herbalwitch.companions.familiars.showcase.FamiliarShowcaseDisplay.prepare(mob, sample.appearance());
            }
            complete = true; return new Result(created.size(), true);
        } catch (RuntimeException failure) {
            LOGGER.error("Showcase sample failed; block construction remains", failure);
            return new Result(0, false);
        } finally { if (!complete) created.forEach(Mob::discard); }
    }
}
