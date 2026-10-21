package io.github.dirtyredz.herbalwitch.showcase.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.dirtyredz.herbalwitch.showcase.construction.ShowcaseBuilder;
import io.github.dirtyredz.herbalwitch.showcase.construction.ShowcasePreflight;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlan;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;

/**
 * {@code /herbalwitch showcase [section]}: builds every registered section (or one) on the flat
 * ground in front of the executing player. The box starts two blocks east of the player, on the
 * player's row, with its floor at the block the player stands on; the player stays outside it.
 */
public final class ShowcaseCommand {
    /** Distance east of the player to the box, leaving an entrance aisle. */
    public static final int ENTRANCE = 2;

    private ShowcaseCommand() {
    }

    /** Minimum corner of the box for a player standing at {@code feet}. */
    public static BlockPos origin(BlockPos feet) {
        return new BlockPos(feet.getX() + ENTRANCE, feet.getY() - 1, feet.getZ());
    }

    public static int run(CommandSourceStack source, Optional<String> sectionId) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        List<ShowcaseSection> sections;
        if (sectionId.isPresent()) {
            Optional<ShowcaseSection> one = ShowcaseRegistry.byId(sectionId.get());
            if (one.isEmpty()) {
                source.sendFailure(Component.translatable(ShowcasePreflight.KEY + "unknown_section", sectionId.get()));
                return 0;
            }
            sections = List.of(one.get());
        } else {
            sections = ShowcaseRegistry.sections();
        }
        ShowcasePlan plan;
        try {
            plan = ShowcasePlan.of(sections);
        } catch (ShowcasePlan.InvalidPlanException e) {
            source.sendFailure(Component.translatable(ShowcasePreflight.KEY + "too_large", e.getMessage()));
            return 0;
        }
        ServerLevel level = player.serverLevel();
        BlockPos origin = origin(player.blockPosition());
        Optional<Component> problem = ShowcasePreflight.check(plan, level, player, origin);
        if (problem.isPresent()) {
            source.sendFailure(problem.get());
            return 0;
        }
        ShowcaseBuilder.Result result = ShowcaseBuilder.build(plan, level, origin);
        BlockPos max = origin.offset(plan.width() - 1, ShowcasePlan.HEIGHT - 1, plan.depth() - 1);
        source.sendSuccess(() -> Component.translatable(ShowcasePreflight.KEY + "success", sections.size(),
                result.placed(), result.cleared(), pos(origin), pos(max)), true);
        return result.placed();
    }

    private static String pos(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }
}
