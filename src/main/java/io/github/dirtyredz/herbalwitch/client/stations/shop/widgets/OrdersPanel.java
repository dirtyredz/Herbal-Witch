package io.github.dirtyredz.herbalwitch.client.stations.shop.widgets;

import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopProductPolicy;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopRequest;
import io.github.dirtyredz.herbalwitch.client.stations.shop.ShopScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.UUID;

/**
 * Nonbinding requests. A customer picks a tradeable product, quantity and the most they will pay per
 * lot (no payment, reservation or promise is created) and later buys an offer the owner associated, at
 * its current price. The owner sees every request and declines or associates a compatible offer.
 */
public final class OrdersPanel implements ShopPanel {
    private static final int ROWS = 4, TOP = 82;
    private int product, offer;
    private String quantity = "1", price = "1";
    private boolean plain;

    @Override
    public String id() {
        return "orders";
    }

    private static ItemStack item(String id) {
        var item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(id));
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    @Override
    public void build(ShopScreen screen, int x, int y, CompoundTag snapshot) {
        boolean owner = snapshot.getBoolean("owner");
        List<String> ids = ShopProductPolicy.ids();
        product = Math.floorMod(product, Math.max(1, ids.size()));
        if (!owner) {
            screen.button(x, y + 16, 14, Component.literal("<"), () -> { product--; screen.refresh(); });
            screen.button(x + 36, y + 16, 14, Component.literal(">"), () -> { product++; screen.refresh(); });
            EditBox q = screen.box(x + 54, y + 16, 24, 2, quantity);
            q.setResponder(v -> quantity = v);
            EditBox p = screen.box(x + 82, y + 16, 24, 2, price);
            p.setResponder(v -> price = v);
            screen.button(x + 110, y + 16, 54, ShopPanel.text(plain ? "plain_only" : "any_metadata"), () -> { plain = !plain; screen.refresh(); });
            screen.button(x + 168, y + 16, 66, ShopPanel.text("request"), () -> screen.send(ShopRequest.Action.ORDER_CREATE, null, null, 0,
                    ShopScreen.parse(quantity, 0), ShopScreen.parse(price, 0), 0, plain, ids.get(product))).active = !snapshot.getBoolean("banned");
        }
        List<CompoundTag> orders = ShopPanel.list(snapshot, "orders");
        List<CompoundTag> offers = ShopPanel.list(snapshot, "offers");
        for (int i = 0; i < Math.min(ROWS, orders.size()); i++) {
            CompoundTag order = orders.get(i);
            UUID id = order.getUUID("id");
            String state = order.getString("state");
            boolean active = state.equals("OPEN") || state.equals("OFFERED");
            int by = screen.top() + TOP + i * 22 + 3;
            if (owner) {
                screen.button(x + 150, by, 40, ShopPanel.text("decline"), () -> screen.send(ShopRequest.Action.ORDER_DECLINE, id, null, 0, 0, 0, 0, false, "")).active = active;
                offer = Math.min(offer, Math.max(0, offers.size() - 1));
                UUID choice = offers.isEmpty() ? null : offers.get(offer).getUUID("id");
                screen.button(x + 194, by, 40, ShopPanel.text("offer"), () -> screen.send(ShopRequest.Action.ORDER_OFFER, id, choice, 0, 0, 0, 0, false, ""))
                        .active = active && choice != null;
            } else {
                screen.button(x + 150, by, 40, ShopPanel.text("cancel"), () -> screen.send(ShopRequest.Action.ORDER_CANCEL, id, null, 0, 0, 0, 0, false, "")).active = active;
                if (state.equals("OFFERED") && order.hasUUID("offer")) {
                    UUID offerId = order.getUUID("offer");
                    long revision = order.getLong("offerRevision");
                    screen.button(x + 194, by, 40, ShopPanel.text("buy"), () -> screen.send(ShopRequest.Action.BUY, offerId, id, revision, 0, 0, 0, false, ""));
                }
            }
        }
        if (owner && !offers.isEmpty()) {
            screen.button(x + 150, y + 16, 84, Component.literal(screen.font().plainSubstrByWidth(
                    ShopPanel.stack(offers.get(Math.min(offer, offers.size() - 1)), "stack").getHoverName().getString(), 78)), () -> {
                offer = (offer + 1) % offers.size();
                screen.refresh();
            });
        }
    }

    @Override
    public void labels(ShopScreen screen, GuiGraphics graphics, CompoundTag snapshot, int mouseX, int mouseY) {
        var font = screen.font();
        boolean owner = snapshot.getBoolean("owner");
        graphics.drawString(font, ShopPanel.text(owner ? "orders_owner" : "orders_customer"), 8, 34, ShopScreen.INK, false);
        if (!owner) {
            List<String> ids = ShopProductPolicy.ids();
            if (!ids.isEmpty()) ShopPanel.item(screen, graphics, item(ids.get(Math.floorMod(product, ids.size()))), 23, 47);
            graphics.drawString(font, ShopPanel.text("order_fields"), 62, 66, ShopScreen.MUTED, false);
            graphics.drawString(font, ShopPanel.text("nonbinding"), 8, 72, ShopScreen.ALERT, false);
        } else {
            graphics.drawString(font, ShopPanel.text("associate_hint"), 8, 50, ShopScreen.MUTED, false);
        }
        List<CompoundTag> orders = ShopPanel.list(snapshot, "orders");
        if (orders.isEmpty()) graphics.drawString(font, ShopPanel.text("no_orders"), 8, TOP + 4, ShopScreen.MUTED, false);
        for (int i = 0; i < Math.min(ROWS, orders.size()); i++) {
            CompoundTag order = orders.get(i);
            int ry = TOP + i * 22;
            graphics.blit(ShopScreen.ATLAS, 8, ry, 0, 204, 140, 20);
            ShopPanel.item(screen, graphics, item(order.getString("item")), 9, ry + 1);
            graphics.drawString(font, font.plainSubstrByWidth(order.getString("requester"), 112), 28, ry + 2, ShopScreen.INK, false);
            graphics.drawString(font, ShopPanel.text("order_line", order.getInt("purchased"), order.getInt("quantity"), order.getInt("maxPrice"),
                    ShopPanel.text("order_state." + order.getString("state").toLowerCase(java.util.Locale.ROOT))), 28, ry + 11, ShopScreen.MUTED, false);
        }
    }

    @Override
    public void tooltips(ShopScreen screen, GuiGraphics graphics, CompoundTag snapshot, int mouseX, int mouseY) {
        if (!snapshot.getBoolean("owner")) {
            List<String> ids = ShopProductPolicy.ids();
            if (!ids.isEmpty()) ShopPanel.tooltip(screen, graphics, item(ids.get(Math.floorMod(product, ids.size()))), 23, 47, mouseX, mouseY);
        }
        List<CompoundTag> orders = ShopPanel.list(snapshot, "orders");
        for (int i = 0; i < Math.min(ROWS, orders.size()); i++) {
            ShopPanel.tooltip(screen, graphics, item(orders.get(i).getString("item")), 9, TOP + i * 22 + 1, mouseX, mouseY);
        }
    }
}
