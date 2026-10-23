package io.github.dirtyredz.herbalwitch.showcase.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.dirtyredz.herbalwitch.showcase.construction.ShowcaseBuilder;
import io.github.dirtyredz.herbalwitch.showcase.construction.ShowcasePreflight;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseGroup;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlan;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * {@code /herbalwitch showcase [furniture|witchcraft|section]}: builds the furniture showcase and the
 * witchcraft showcase (herbs, stations, products, magic) each on its own plot, or one group, or one
 * section, in front of the executing player, replacing the terrain there with its own floor. The first box
 * starts two blocks east of the player, on the player's row, with its floor at the block the player
 * stands on; the player stays outside it. With no argument the second showcase's box lies
 * {@link #SEPARATION} blocks south of the first. Every box is checked before any is built.
 */
public final class ShowcaseCommand {
    /** Distance east of the player to the box, leaving an entrance aisle. */
    public static final int ENTRANCE = 2;
    /** Open ground between the two showcases of a bare command. */
    public static final int SEPARATION = 8;

    private ShowcaseCommand() {
    }

    /** Minimum corner of the box for a player standing at {@code feet}. */
    public static BlockPos origin(BlockPos feet) {
        return new BlockPos(feet.getX() + ENTRANCE, feet.getY() - 1, feet.getZ());
    }

    public static int run(CommandSourceStack source, Optional<String> argument) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        List<List<ShowcaseSection>> showcases = new ArrayList<>();
        if (argument.isEmpty()) {
            for (ShowcaseGroup group : ShowcaseGroup.values()) {
                List<ShowcaseSection> sections = ShowcaseRegistry.sections(group);
                if (!sections.isEmpty()) {
                    showcases.add(sections);
                }
            }
        } else if (ShowcaseRegistry.group(argument.get()).isPresent()) {
            showcases.add(ShowcaseRegistry.sections(ShowcaseRegistry.group(argument.get()).get()));
        } else {
            Optional<ShowcaseSection> one = ShowcaseRegistry.byId(argument.get());
            if (one.isEmpty()) {
                source.sendFailure(Component.translatable(ShowcasePreflight.KEY + "unknown_section", argument.get()));
                return 0;
            }
            showcases.add(List.of(one.get()));
        }
        ServerLevel level = player.serverLevel();
        List<ShowcasePlan> plans = new ArrayList<>();
        List<BlockPos> origins = new ArrayList<>();
        BlockPos origin = origin(player.blockPosition());
        for (List<ShowcaseSection> sections : showcases) {
            ShowcasePlan plan;
            try {
                plan = ShowcasePlan.of(sections);
            } catch (ShowcasePlan.InvalidPlanException e) {
                source.sendFailure(Component.translatable(ShowcasePreflight.KEY + "too_large", e.getMessage()));
                return 0;
            }
            Optional<Component> problem = ShowcasePreflight.check(plan, level, player, origin);
            if (problem.isPresent()) {
                source.sendFailure(problem.get());
                return 0;
            }
            plans.add(plan);
            origins.add(origin);
            origin = origin.offset(0, 0, plan.depth() + SEPARATION);
        }
        int placed = 0;
        for (int i = 0; i < plans.size(); i++) {
            ShowcasePlan plan = plans.get(i);
            BlockPos at = origins.get(i);
            ShowcaseBuilder.Result result = ShowcaseBuilder.build(plan, level, player, at);
            if (!result.complete()) {
                source.sendFailure(Component.translatable(ShowcasePreflight.KEY + "samples_failed", result.placed()));
                return 0;
            }
            BlockPos max = at.offset(plan.width() - 1, ShowcasePlan.HEIGHT - 1, plan.depth() - 1);
            int sections = showcases.get(i).size();
            source.sendSuccess(() -> Component.translatable(ShowcasePreflight.KEY + "success", sections,
                    result.placed(), result.cleared(), pos(at), pos(max)), true);
            if (result.entities() > 0) {
                source.sendSuccess(() -> Component.translatable(ShowcasePreflight.KEY + "samples", result.entities()), true);
            }
            placed += result.placed();
        }
        return placed;
    }

    private static String pos(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }
}
