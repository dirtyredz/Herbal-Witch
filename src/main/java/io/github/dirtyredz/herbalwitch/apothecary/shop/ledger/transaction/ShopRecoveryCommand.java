package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction;

import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStore;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.UUID;

/**
 * Permission-level-two inspection and explicit resolution of retained commerce transactions:
 * {@code /herbalwitch shop inspect <shop>}, {@code ... shop transaction inspect|reconcile|discard <shop>
 * <transaction>}. Reconcile acts only on proven endpoint state; discard drops the shop-side reservation
 * without granting items or refunds, records the loss and retires the identity. No command accepts an
 * item payload or a balance.
 */
public final class ShopRecoveryCommand {
    private ShopRecoveryCommand() {
    }

    public static void register(RegisterCommandsEvent event) {
        var root = Commands.literal("shop").requires(source -> source.hasPermission(2));
        root.then(Commands.literal("inspect").then(Commands.argument("shop", UuidArgument.uuid()).executes(context -> {
            UUID shop = UuidArgument.getUuid(context, "shop");
            ShopStore store = ShopStore.get(context.getSource().getServer());
            String text = store.record(shop).map(ShopRecoveryCommand::describe)
                    .orElseGet(() -> store.problem(shop).map(p -> "Shop shard unavailable (" + p + "); file preserved").orElse("No shop record"));
            context.getSource().sendSuccess(() -> Component.literal(text), false);
            return store.record(shop).isPresent() ? 1 : 0;
        })));
        var transaction = Commands.literal("transaction");
        transaction.then(Commands.literal("inspect").then(Commands.argument("shop", UuidArgument.uuid()).then(Commands.argument("transaction", UuidArgument.uuid())
                .executes(context -> inspect(context.getSource(), UuidArgument.getUuid(context, "shop"), UuidArgument.getUuid(context, "transaction"))))));
        transaction.then(Commands.literal("reconcile").then(Commands.argument("shop", UuidArgument.uuid()).then(Commands.argument("transaction", UuidArgument.uuid())
                .executes(context -> {
                    var outcome = ShopReconciliation.operator(context.getSource().getServer(), UuidArgument.getUuid(context, "shop"), UuidArgument.getUuid(context, "transaction"));
                    boolean resolved = outcome.isPresent() && outcome.get() != ShopRecovery.Outcome.QUARANTINE;
                    context.getSource().sendSuccess(() -> Component.literal(resolved ? "Transaction resolved from proven evidence: " + outcome.get()
                            : "Not proven; the transaction stays quarantined. Inspect the player's saved data or discard it."), true);
                    return resolved ? 1 : 0;
                }))));
        transaction.then(Commands.literal("discard").then(Commands.argument("shop", UuidArgument.uuid()).then(Commands.argument("transaction", UuidArgument.uuid())
                .executes(context -> {
                    UUID operator = context.getSource().getEntity() == null ? new UUID(0, 0) : context.getSource().getEntity().getUUID();
                    boolean discarded = ShopReconciliation.discard(context.getSource().getServer(), UuidArgument.getUuid(context, "shop"),
                            UuidArgument.getUuid(context, "transaction"), operator);
                    context.getSource().sendSuccess(() -> Component.literal(discarded
                            ? "Reservation discarded and recorded as lost; nothing was granted or refunded." : "Discard refused: no such retained transaction or the shard is read-only."), true);
                    return discarded ? 1 : 0;
                }))));
        root.then(transaction);
        event.getDispatcher().register(Commands.literal("herbalwitch").then(root));
    }

    private static int inspect(CommandSourceStack source, UUID shop, UUID transaction) {
        var t = ShopStore.get(source.getServer()).record(shop).flatMap(r -> r.transaction(transaction)).orElse(null);
        if (t == null) {
            source.sendSuccess(() -> Component.literal("No retained transaction"), false);
            return 0;
        }
        String text = t.id() + " " + t.kind() + " " + t.phase() + " actor=" + t.actor() + " sequence=" + t.sequence() + " reason=" + t.reason()
                + " stockOut=" + t.stockOut().stream().mapToInt(ShopTransaction.Goods::count).sum()
                + " stockIn=" + t.stockIn().stream().mapToInt(ShopTransaction.Goods::count).sum()
                + " proceedsOut=" + t.proceedsOut() + " proceedsIn=" + t.proceedsIn() + " slots=" + t.slots().size();
        source.sendSuccess(() -> Component.literal(text), false);
        return 1;
    }

    private static String describe(ShopRecord r) {
        int stock = r.stock.slots().stream().mapToInt(s -> s.count()).sum();
        return r.id + " '" + r.name + "' owner=" + r.owner + " lifecycle=" + r.lifecycle + " revision=" + r.revision + " generation=" + r.generation
                + " stock=" + stock + " proceeds=" + r.proceeds + " offers=" + r.offers.size() + " links=" + r.links.size()
                + " transactions=" + r.transactions.stream().map(t -> t.id() + ":" + t.phase()).toList();
    }
}
