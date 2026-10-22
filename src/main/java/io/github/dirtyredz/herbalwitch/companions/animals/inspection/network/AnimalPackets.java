package io.github.dirtyredz.herbalwitch.companions.animals.inspection.network;

import io.github.dirtyredz.herbalwitch.companions.animals.inspection.menu.AnimalMenu;
import io.github.dirtyredz.herbalwitch.platform.network.Messages;
import net.minecraft.server.level.ServerPlayer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class AnimalPackets {
    private static Consumer<AnimalSnapshot> client = ignored -> {};
    private static BiConsumer<ServerPlayer, AnimalSnapshot> observer;
    private AnimalPackets() {}
    public static void register() {
        Messages.toServer(AnimalRequest.class, AnimalRequest::encode, AnimalRequest::decode, AnimalMenu::request);
        Messages.toClient(AnimalSnapshot.class, AnimalSnapshot::encode, AnimalSnapshot::decode, message -> client.accept(message));
    }
    public static void client(Consumer<AnimalSnapshot> consumer) { client = consumer; }
    public static void observe(BiConsumer<ServerPlayer, AnimalSnapshot> consumer) { observer = consumer; }
    public static void send(ServerPlayer player, AnimalSnapshot snapshot) {
        if (observer != null) observer.accept(player, snapshot);
        Messages.send(player, snapshot);
    }
}
