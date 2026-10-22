package io.github.dirtyredz.herbalwitch.companions.animals.capture.command;

import io.github.dirtyredz.herbalwitch.companions.animals.capture.CaptureService;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureSavedData;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class CaptureCommands {
    private CaptureCommands() {}
    public static void register(RegisterCommandsEvent event) {
        var root = Commands.literal("capture").requires(source -> source.hasPermission(2));
        root.then(Commands.literal("inspect").then(Commands.argument("animal", UuidArgument.uuid()).executes(context -> {
            var data = CaptureSavedData.get(context.getSource().getServer());
            var record = data.record(UuidArgument.getUuid(context, "animal"));
            context.getSource().sendSuccess(() -> Component.literal(record.map(r -> r.animal() + " " + r.phase() + " generation=" + r.generation()
                    + " captor=" + r.captor() + " type=" + r.type() + " source=" + r.source() + " destination=" + r.destination()
                    + " payload=" + r.storedPayload()).orElse("No capture record") + "; writable=" + data.writable()), false);
            return record.isPresent() ? 1 : 0;
        })));
        for (boolean recovery : new boolean[] {false, true}) root.then(Commands.literal(recovery ? "recover" : "reissue")
                .then(Commands.argument("animal", UuidArgument.uuid()).then(Commands.argument("player", EntityArgument.player()).executes(context -> {
                    String result = CaptureService.issue(EntityArgument.getPlayer(context, "player"), UuidArgument.getUuid(context, "animal"), recovery);
                    if (result != null) { context.getSource().sendFailure(Component.translatable("animal.herbalwitch.capture." + result)); return 0; }
                    context.getSource().sendSuccess(() -> Component.translatable("animal.herbalwitch.capture.issued"), true); return 1;
                }))));
        event.getDispatcher().register(Commands.literal("herbalwitch").then(root));
    }
}
