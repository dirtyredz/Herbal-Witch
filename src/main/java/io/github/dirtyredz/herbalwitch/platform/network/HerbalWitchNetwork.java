package io.github.dirtyredz.herbalwitch.platform.network;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The mod's one network channel, {@code herbalwitch:main}, and its protocol version. Features
 * register their own bounded messages on it ({@link #nextId()}); this class holds no gameplay
 * policy. A client and server with different protocol versions refuse to connect, rather than
 * misreading each other's messages.
 */
public final class HerbalWitchNetwork {
    /** Bump whenever any message's wire format changes. */
    public static final String PROTOCOL = "5";
    public static final ResourceLocation CHANNEL_ID = ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "main");
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(CHANNEL_ID, () -> PROTOCOL,
            PROTOCOL::equals, PROTOCOL::equals);
    private static final AtomicInteger IDS = new AtomicInteger();

    private HerbalWitchNetwork() {
    }

    /** The next free message discriminator; features take theirs while registering at bootstrap. */
    public static int nextId() {
        return IDS.getAndIncrement();
    }
}
