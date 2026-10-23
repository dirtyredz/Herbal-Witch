package io.github.dirtyredz.herbalwitch.companions.familiars.admin;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.recovery.FamiliarRecovery;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class FamiliarRecoveryCommand {
    private FamiliarRecoveryCommand() {}
    public static void register(RegisterCommandsEvent event) {
        var root = Commands.literal("familiar").requires(source -> source.hasPermission(2));
        root.then(Commands.literal("inspect").then(Commands.argument("bond", UuidArgument.uuid()).executes(context -> {
            var store = FamiliarStore.get(context.getSource().getServer());
            var record = store.record(UuidArgument.getUuid(context, "bond"));
            context.getSource().sendSuccess(() -> Component.literal(record.map(r -> r.bond() + " " + r.phase() + " owner=" + r.owner()
                    + " creature=" + r.creature() + " generation=" + r.generation() + " payload=" + !r.payload().isEmpty()
                    + " unresolvedReceipt=" + r.state().contains("receipt") + " writable=" + store.writable(r.bond())).orElse("No familiar record")), false);
            return record.isPresent() ? 1 : 0;
        })));
        root.then(Commands.literal("recover").then(Commands.argument("bond", UuidArgument.uuid()).executes(context -> {
            boolean recovered = FamiliarRecovery.recover(context.getSource().getServer(), UuidArgument.getUuid(context, "bond"));
            context.getSource().sendSuccess(() -> Component.literal(recovered ? "Retained familiar is now incapacitated; owner revival is required."
                    : "Recovery refused: inspect the retained payload, phase, unresolved receipt and writable state."), true);
            return recovered ? 1 : 0;
        })));
        event.getDispatcher().register(Commands.literal("herbalwitch").then(root));
    }
}
