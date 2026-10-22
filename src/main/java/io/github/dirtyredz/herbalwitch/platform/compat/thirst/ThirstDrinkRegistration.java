package io.github.dirtyredz.herbalwitch.platform.compat.thirst;

import com.mojang.logging.LogUtils;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.ProductSpec;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.slf4j.Logger;

import java.lang.reflect.Method;

/**
 * Registers the seven kettle drinks with Thirst (hydration 6, quenched 3) through its
 * {@code RegisterThirstValueEvent.addDrink(Item, int, int)}, posted on the Forge bus when Thirst
 * builds its drink table at server start. Reached by name, so HerbalWitch compiles and ships
 * without Thirst classes; a missing or changed API is logged and Thirst support stays off.
 */
public final class ThirstDrinkRegistration {
    public static final String EVENT = "dev.ghen.thirst.foundation.common.event.RegisterThirstValueEvent";
    public static final int HYDRATION = 6;
    public static final int QUENCHED = 3;
    private static final Logger LOGGER = LogUtils.getLogger();

    private ThirstDrinkRegistration() {
    }

    static void register() {
        try {
            Class<? extends Event> type = Class.forName(EVENT).asSubclass(Event.class);
            Method addDrink = type.getMethod("addDrink", Item.class, int.class, int.class);
            listen(type, addDrink);
        } catch (ReflectiveOperationException | ClassCastException e) {
            LOGGER.warn("Thirst Was Taken is present but its drink registration event was not found; teas will not hydrate", e);
        }
    }

    private static <T extends Event> void listen(Class<T> type, Method addDrink) {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, type, event -> {
            for (ProductSpec drink : EverydayProductCatalog.DRINKS) {
                try {
                    addDrink.invoke(event, PreparationItems.product(drink), HYDRATION, QUENCHED);
                } catch (ReflectiveOperationException e) {
                    LOGGER.warn("Could not register {} with Thirst", drink.id(), e);
                }
            }
        });
    }
}
