package io.github.dirtyredz.herbalwitch.showcase.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.Optional;

/**
 * Registers {@code /herbalwitch showcase [section]}. Operators only (permission level 2); creative
 * mode alone is not enough, and the console is refused (a player origin is required).
 */
public final class ShowcaseCommandRegistration {
    public static final int PERMISSION_LEVEL = 2;

    private ShowcaseCommandRegistration() {
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(ShowcaseCommandRegistration::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(HerbalWitch.MOD_ID)
                .then(Commands.literal("showcase")
                        .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                        .executes(context -> ShowcaseCommand.run(context.getSource(), Optional.empty()))
                        .then(Commands.argument("section", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        ShowcaseRegistry.sections().stream().map(ShowcaseSection::id), builder))
                                .executes(context -> ShowcaseCommand.run(context.getSource(),
                                        Optional.of(StringArgumentType.getString(context, "section")))))));
    }
}
