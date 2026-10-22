package io.github.dirtyredz.herbalwitch.magic.witch.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraftforge.event.RegisterCommandsEvent;

/**
 * Registers the operator commands (permission level 2) under {@code /herbalwitch witch}:
 * {@code inspect <player>}, {@code designate <player>}, {@code founding inspect} and
 * {@code founding resolve <claim> close}. Brigadier merges this {@code herbalwitch} root with the
 * showcase command's.
 */
public final class WitchCommandRegistration {
    public static final int PERMISSION_LEVEL = 2;

    private WitchCommandRegistration() {
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(HerbalWitch.MOD_ID)
                .then(Commands.literal("witch")
                        .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                        .then(Commands.literal("inspect").then(Commands.argument("player", EntityArgument.player())
                                .executes(c -> WitchCommand.inspect(c.getSource(), EntityArgument.getPlayer(c, "player")))))
                        .then(Commands.literal("designate").then(Commands.argument("player", EntityArgument.player())
                                .executes(c -> WitchCommand.designate(c.getSource(), EntityArgument.getPlayer(c, "player")))))
                        .then(Commands.literal("founding")
                                .then(Commands.literal("inspect").executes(c -> WitchCommand.foundingInspect(c.getSource())))
                                .then(Commands.literal("resolve").then(Commands.argument("claim", UuidArgument.uuid())
                                        .then(Commands.literal("close")
                                                .executes(c -> WitchCommand.foundingResolve(c.getSource(), UuidArgument.getUuid(c, "claim")))))))));
    }
}
