package io.github.dirtyredz.herbalwitch.companions.familiars.network;

import io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarRosterMenu;
import io.github.dirtyredz.herbalwitch.platform.network.Messages;
import net.minecraft.server.level.ServerPlayer;
import java.util.function.Consumer;
import java.util.function.BiConsumer;

public final class FamiliarPackets {
    private static Consumer<FamiliarObservations> observations = ignored -> {};
    public static void observations(Consumer<FamiliarObservations> consumer) { observations = consumer; }
    private static Consumer<FamiliarVisualSnapshot> visual = ignored -> {};
    public static void visuals(Consumer<FamiliarVisualSnapshot> consumer) { visual = consumer; }
    private static Consumer<FamiliarRosterPage> client = ignored -> {};
    private static BiConsumer<ServerPlayer, FamiliarRosterPage> observer;
    private FamiliarPackets() {}
    public static void register() {
        Messages.toClient(FamiliarVisualSnapshot.class, FamiliarVisualSnapshot::encode, FamiliarVisualSnapshot::decode, value -> visual.accept(value));
        Messages.toServer(FamiliarRequest.class, FamiliarRequest::encode, FamiliarRequest::decode, FamiliarRosterMenu::request);
        Messages.toClient(FamiliarRosterPage.class, FamiliarRosterPage::encode, FamiliarRosterPage::decode, page -> client.accept(page));
        Messages.toClient(FamiliarObservations.class, FamiliarObservations::encode, FamiliarObservations::decode, page -> observations.accept(page));
    }
    public static void client(Consumer<FamiliarRosterPage> consumer) { client = consumer; }
    public static void observe(BiConsumer<ServerPlayer, FamiliarRosterPage> consumer) { observer = consumer; }
    public static void send(ServerPlayer owner, FamiliarRosterPage page) {
        if (observer != null) observer.accept(owner, page);
        Messages.send(owner, page);
    }
    public static void request(FamiliarRequest request) { Messages.sendToServer(request); }
}
